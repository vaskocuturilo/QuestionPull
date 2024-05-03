package com.example.questionpull.controller;

import com.example.questionpull.StorageUtils;
import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.service.QuestionPullServiceImplementation;
import com.example.questionpull.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static com.example.questionpull.factory.KeyboardFactory.addButtonAndSendMessage;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;


    private final QuestionPullServiceImplementation questionPullService;
    private final StorageUtils storageUtils;


    private static final String NEXT_QUESTION = "NEXT_QUESTION";
    private static final String STOP_QUESTION = "STOP_QUESTION";

    @Value("${bot.message.end.question}")
    String messages;

    public UpdateController(QuestionPullServiceImplementation questionPullService, StorageUtils storageUtils) {
        this.questionPullService = questionPullService;
        this.storageUtils = storageUtils;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void proceedMessage(Update update) {
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
                    var question = getQuestionFromPull("easy");
                    logic(question, chatId);
                }

                default -> commandNotFound(chatId);
            }
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(NEXT_QUESTION)) {
                var question = getQuestionFromPull("easy");
                logic(question, chatId);
            }

            if (callbackData.equals(STOP_QUESTION)) {
                stopChat(chatId);
            }
        }
    }

    private Optional<QuestionPullEntity> getQuestionFromPull(final String level) {
        return questionPullService.getRandomQuestion(level);
    }

    public void showStart(long chatId, String name) {
        String answer = "Hi, " + name + ", Nice to meet you! You can use \"/question\" command for start question pull or \"/help\" for more information.";
        sendMessage(answer, chatId);
    }

    private void sendMessage(String textToSend, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        telegramBot.send(message);
    }

    public void commandNotFound(long chatId) {
        String answer = "This is not a recognized command. You can use \"/help\" for more information.";
        sendMessage(answer, chatId);
    }

    public void helpCommand(long chatId) {
        String answer = "You can use \"/question\" command for start question pull.";
        sendMessage(answer, chatId);
    }

    public void stopChat(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messages);
        telegramBot.send(message);
    }

    private void logic(Optional<QuestionPullEntity> question, long chatId) {
        question.ifPresent(questionPullEntity -> telegramBot.send(addButtonAndSendMessage(
                """
                        Title: %s
                        Body: %s
                        Example: %s
                        """.formatted(questionPullEntity.getTitle(), questionPullEntity.getBody(), questionPullEntity.getExample()), chatId, NEXT_QUESTION)));

        question.ifPresentOrElse(questionPullEntity -> questionPullService.setActiveForQuestion(questionPullEntity.getUuid()), () -> stopChat(chatId));
    }
}
