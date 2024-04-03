package com.example.questionpull.controller;

import com.example.questionpull.config.BotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    Logger logger = Logger.getLogger("BotInitializer");

    private BotProperties config;

    @Autowired
    public TelegramBot(BotProperties config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            logger.log(Level.INFO, () -> "Message from bot: " + messageText);
        }
    }
}
