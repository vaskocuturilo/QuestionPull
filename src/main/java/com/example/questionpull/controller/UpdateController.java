package com.example.questionpull.controller;

import com.example.questionpull.entity.UserEntity;
import com.example.questionpull.service.TelegramBot;
import com.example.questionpull.service.questions.QuestionPullImplementation;
import com.example.questionpull.service.questions.QuestionPullService;
import com.example.questionpull.service.users.UserService;
import com.example.questionpull.util.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final QuestionPullImplementation questionPullService;
    private final QuestionPullService service;
    private final UserService userService;


    @Value("${bot.message.end.questions}")
    String endMessage;

    @Value("${bot.message.stop.questions}")
    String stopQuiz;

    @Value("${bot.message.change.level}")
    String changeLevel;

    public UpdateController(QuestionPullImplementation questionPullService,
                            QuestionPullService service, UserService userService) {
        this.questionPullService = questionPullService;
        this.service = service;
        this.userService = userService;
    }

    public void registerBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void proceedMessage(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(final Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        switch (messageText) {
            case "/start" -> handleStartCommand(chatId, update.getMessage().getChat().getFirstName());
            case "/help" -> handleHelpCommand(chatId);
            case "/question" -> sendNextQuestion(chatId, "easy");
            default -> service.createCustomMessage(chatId);
        }
    }

    private void sendNextQuestion(final long chatId, String level) {
        UserEntity user = userService.findOrCreateUser(chatId, "");
        List<UUID> history = user.getHistoryArray() != null ? user.getHistoryArray() : new ArrayList<>();

        questionPullService.getRandomQuestionExcludingIds(level, history).ifPresentOrElse(question -> {
            history.add(question.getUuid());
            user.setCurrentQId(question.getUuid());
            user.setHistoryArray(history);
            userService.updateUser(user);

            service.sendQuestionMessage(question, chatId, level);
        }, () -> service.sendStopMessage(chatId, endMessage));
    }

    private void handleHelpCommand(long chatId) {
        final SendMessage sendMessage = service.createCustomMessage(chatId);
        String answer = "You can use menu:";
        sendMessage.setText(answer);
        telegramBot.send(sendMessage);
    }

    private void handleStartCommand(final long chatId, final String name) {
        final SendMessage sendMessage = service.createCustomMessage(chatId);
        String answer = "Hi, " + name + ", Nice to meet you! You can use menu or 'Help & Info' button for more information.";
        sendMessage.setText(answer);
        telegramBot.send(sendMessage);
    }

    private void handleCallbackQuery(Update update) {
        String callBackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        CallbackData.fromString(callBackData)
                .ifPresentOrElse(callback -> callbackHandlers.getOrDefault(callback, this::handleUnhandledCallback).accept(chatId),
                        () -> log.warn("Invalid callback data: {}", callBackData)
                );
    }

    private final Map<CallbackData, Consumer<Long>> callbackHandlers = Map.of(
            CallbackData.NEXT_QUESTION_EASY, chatId -> sendNextQuestion(chatId, "easy"),
            CallbackData.NEXT_QUESTION_MEDIUM, chatId -> sendNextQuestion(chatId, "medium"),
            CallbackData.NEXT_QUESTION_HARD, chatId -> sendNextQuestion(chatId, "hard"),
            CallbackData.BUTTON_PASS, this::handleChangeLevelCommand,
            CallbackData.BUTTON_FAIL, this::handleChangeLevelCommand,
            CallbackData.CHANGE_LEVEL, this::handleChangeLevelCommand,
            CallbackData.STOP_QUESTION, this::handleStopCommand,
            CallbackData.HELP, this::handleHelpCommand
    );

    private void handleUnhandledCallback(long chatId) {
        log.warn("Unhandled callback query for chatId: {}", chatId);
    }

    private void handleStopCommand(long chatId) {
        userService.resetUserQuestions(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(stopQuiz);
        telegramBot.send(message);
    }

    private void handleChangeLevelCommand(long chatId) {
        final SendMessage sendMessage = service.createChangeLevelMessage(chatId);
        sendMessage.setChatId(chatId);
        sendMessage.setText(changeLevel);
        telegramBot.send(sendMessage);
    }
}
