package com.monitoring.monitor.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monitoring.monitor.model.interfaces.UserRepository;
import com.monitoring.monitor.service.UserService;

import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;

@Component
@Log4j2
public class MessageController extends TelegramLongPollingBot {
    private final UserService userService;
    public MessageController(@Value("${BOT_TOKEN}") String botToken, UserService userService) {
        super(botToken);
        this.userService = userService;
    }

    private void sendMessage(long id, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(id);
        message.setText(text);
        execute(message);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (message) {
                case "/start":
                    try {
                        sendMessage(chatId, "Hello " + update.getMessage().getFrom().getFirstName() + "!");
                        userService.addUserToDatabase(update.getMessage().getFrom());

                    } catch (TelegramApiException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "/startime":
                    try {
                        sendMessage(chatId, "Вы начали смену!");
                    } catch (TelegramApiException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                default:
                    try {
                        sendMessage(chatId, "Unknown command...");
                    } catch (TelegramApiException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
            }
        } else if (update.hasMessage() && update.getMessage().hasVoice()) {
            Voice voice = update.getMessage().getVoice();
            try {
                // 1. Скачиваем голосовое сообщение
                String fileId = voice.getFileId();
                GetFile getFile = new GetFile(fileId);
                String filePath = execute(getFile).getFilePath();
                File voiceFile = downloadFile(filePath, new File("voice.ogg"));

                // 2. Конвертируем OGG → WAV (нужна внешняя библиотека, например JAVE2)
                // Здесь нужен конвертер, например: https://github.com/a-schild/jave2
                File wavFile = convertOggToWav(voiceFile);

                // 3. Распознаём речь через Google Cloud Speech-to-Text
                String recognizedText = sendToVosk(wavFile);

                // 4. Отправляем результат пользователю
                SendMessage response = new SendMessage();
                response.setChatId(update.getMessage().getChatId().toString());
                response.setText("Вы сказали: " + recognizedText);
                execute(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                sendMessage(update.getMessage().getChatId(), "Something went wrong...");
            } catch (TelegramApiException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private File convertOggToWav(File oggFile) throws Exception {
        File target = new File("converted.wav");
        try {
            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("pcm_s16le");
            audio.setBitRate(256000);
            audio.setChannels(1);
            audio.setSamplingRate(16000);

            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setInputFormat("Ogg");
            attrs.setOutputFormat("Wav");
            attrs.setAudioAttributes(audio);

            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(oggFile), target, attrs);

        } catch (Exception ex) {
            throw new Exception("Ошибка конвертации: " + ex.getMessage());
        }
        return target;
    }
    private String sendToVosk(File audioFile) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "audio",
                        audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/wav"))
                )
                .build();

        Request request = new Request.Builder()
                .url("http://localhost:5000/recognize")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() && response.body() == null) throw new IOException("Ошибка запроса");

            String jsonResponse = response.body().string();
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            return jsonObject.get("text").getAsString();  // JSON-ответ вида {"text": "..."}
        }
    }

    @Override
    public String getBotUsername() {
        return "HyperLab_bot";
    }
}
