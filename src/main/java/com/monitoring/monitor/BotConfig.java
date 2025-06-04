package com.monitoring.monitor;

import com.monitoring.monitor.controller.MessageController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@Configuration
@EnableAutoConfiguration
public class BotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(MessageController messageController){
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(messageController);
            return telegramBotsApi;
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка инициализации бота", e);
        }
    }
}
