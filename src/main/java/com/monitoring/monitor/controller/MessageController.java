package com.monitoring.monitor.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Log4j2
public class MessageController extends TelegramLongPollingBot {
    public MessageController(@Value("${BOT_TOKEN}") String botToken) {
        super(botToken);
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
        } else {
            try {
                sendMessage(update.getMessage().getChatId(), "Something went wrong...");
            } catch (TelegramApiException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "HyperLab_bot";
    }
}
