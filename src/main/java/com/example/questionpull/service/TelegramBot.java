package com.example.questionpull.service;

import com.example.questionpull.StorageUtils;
import com.example.questionpull.config.BotProperties;
import com.example.questionpull.entity.QuestionPullEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Optional;

import static com.example.questionpull.factory.KeyboardFactory.addButtonAndSendMessage;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties config;

    private final QuestionPullServiceImplementation questionPullService;
    private final StorageUtils storageUtils;

    private static final String NEXT_QUESTION = "NEXT_QUESTION";
    private static final String STOP_QUESTION = "STOP_QUESTION";

    @Value("${bot.message.end.question}")
    String messages;

    public TelegramBot(BotProperties config, QuestionPullServiceImplementation questionPullService, StorageUtils storageUtils) {
        this.config = config;
        this.questionPullService = questionPullService;
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
        send(message);
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
        send(message);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void logic(Optional<QuestionPullEntity> question, long chatId) {
        question.ifPresent(questionPullEntity -> send(addButtonAndSendMessage(
                """
                          Title: %s,
                          Body: %s,
                          Example: %s                
                        """.formatted(questionPullEntity.getTitle(), questionPullEntity.getBody(), questionPullEntity.getExample()), chatId, NEXT_QUESTION)));

        question.ifPresentOrElse(questionPullEntity -> questionPullService.setActiveForQuestion(questionPullEntity.getUuid()), () -> stopChat(chatId));
    }
}
