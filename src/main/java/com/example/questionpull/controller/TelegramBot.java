package com.example.questionpull.controller;

import com.example.questionpull.config.BotProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties config;

    private static final String TEXT = "Test1";

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
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> showStart(chatId, update.getMessage().getChat().getFirstName());
                case "/help" -> helpCommand(chatId);
                case "/exam" -> {
                    var question = getQuestionFromPull();
                    sendMessage(question, chatId);
                }
                default -> commandNotFound(chatId);
            }
        }
    }

    private String getQuestionFromPull() {
        return TEXT;
    }

    private void commandNotFound(long chatId) {
        String answer = "This is not a recognized command. You can use \"/help\" for more information.";
        sendMessage(answer, chatId);
    }

    private void helpCommand(long chatId) {
        String answer = "You can use \"/exam\" command for start question pull.";
        sendMessage(answer, chatId);
    }

    private void sendMessage(String textToSend, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }

    private void showStart(long chatId, String name) {
        String answer = "Hi, " + name + ", Nice to meet you! You can use \"/exam\" command for start question pull or \"/help\" for more information.";
        sendMessage(answer, chatId);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
