package com.example.questionpull.service;

import com.example.questionpull.StorageUtils;
import com.example.questionpull.config.BotProperties;
import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties config;

    private final QuestionPullRepository questionPullRepository;

    private final StorageUtils storageUtils;

    @Autowired
    public TelegramBot(BotProperties config, QuestionPullRepository questionPullRepository, StorageUtils storageUtils) {
        this.config = config;
        this.questionPullRepository = questionPullRepository;
        this.storageUtils = storageUtils;
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
                case "/start" -> {
                    showStart(chatId, update.getMessage().getChat().getFirstName());
                    storageUtils.loadQuestionsPull();
                }
                case "/help" -> helpCommand(chatId);
                case "/question" -> {
                    var question = getQuestionFromPull();
                    question.ifPresentOrElse(questionPullEntity -> sendMessage(
                                    String.format(""" 
                                                    Title: %s,
                                                    Question: %s
                                                    """, questionPullEntity.getTitle(),
                                            questionPullEntity.getBody()), chatId),
                            () -> {
                                throw new IllegalStateException("Can't take any question");
                            });
                }
                default -> commandNotFound(chatId);
            }
        }
    }

    private Optional<QuestionPullEntity> getQuestionFromPull() {
        return questionPullRepository.getRandomQuestion();
    }

    private void commandNotFound(long chatId) {
        String answer = "This is not a recognized command. You can use \"/help\" for more information.";
        sendMessage(answer, chatId);
    }

    private void helpCommand(long chatId) {
        String answer = "You can use \"/question\" command for start question pull.";
        sendMessage(answer, chatId);
    }

    private void sendMessage(String textToSend, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        send(message);
    }

    private void showStart(long chatId, String name) {
        String answer = "Hi, " + name + ", Nice to meet you! You can use \"/question\" command for start question pull or \"/help\" for more information.";
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
