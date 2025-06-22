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

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final QuestionPullImplementation questionPullService;
    private final QuestionPullService service;
    private final UserService userService;
    private final List<String> levels = new ArrayList<>(List.of("easy", "medium", "hard"));
    private static final SecureRandom RANDOM = new SecureRandom();

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
            case "/start" -> {
                handleStartCommand(chatId, update.getMessage().getChat().getFirstName());
                log.info("User started bot: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
            }
            case "/help" -> {
                handleHelpCommand(chatId);
                log.info("User use the help command: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
            }
            case "/stop" -> {
                handleStopCommand(chatId);
                log.info("User use the stop command: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
            }
            default -> handleUnknownCommand(chatId, messageText);
        }
    }

    private void sendNextQuestion(final long chatId, String level) {
        UserEntity user = userService.findOrCreateUser(chatId, "");
        List<UUID> history = user.getHistoryArray() != null ? user.getHistoryArray() : new ArrayList<>();

        questionPullService.getRandomQuestionExcludingIds(level.contains("random") ? levels.get(RANDOM.nextInt(0, 2)) : level, history).ifPresentOrElse(question -> {
            history.add(question.getUuid());
            user.setCurrentQId(question.getUuid());
            user.setHistoryArray(history);
            userService.updateUser(user);

            service.sendQuestionMessage(question, chatId, level);
        }, () -> service.sendStopMessage(chatId, endMessage));
    }

    private void handleHelpCommand(long chatId) {
        final SendMessage sendMessage = service.createCustomMessage(chatId);
        telegramBot.send(sendMessage);
    }

    private void handleStartCommand(final long chatId, final String name) {
        final Map<String, Long> counts = questionPullService.getQuestionCountsByLevel();

        userService.findOrCreateUser(chatId, "");
        final SendMessage sendMessage = service.createCustomMessage(chatId, counts);

        String answer = "Hi, " + name + ", Nice to meet you! You can start any time.";

        sendMessage.setText(answer);
        telegramBot.send(sendMessage);
    }

    private void handleCallbackQuery(Update update) {
        String callBackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callBackData.startsWith("ANSWER_PASS_") || callBackData.startsWith("ANSWER_FAIL_")) {
            handleAnswerCallback(callBackData, chatId);
            return;
        }

        CallbackData.fromString(callBackData)
                .ifPresentOrElse(callback -> callbackHandlers
                                .getOrDefault(callback, this::handleUnhandledCallback)
                                .accept(chatId),
                        () -> log.warn("Invalid callback data: {}", callBackData));

    }

    private void handleAnswerCallback(String callbackData, Long chatId) {
        String[] parts = callbackData.split("_");
        if (parts.length != 3) {
            log.warn("Invalid answer callback format: {}", callbackData);
            return;
        }

        String result = parts[1];
        String level = parts[2].toLowerCase();

        int score = "PASS".equalsIgnoreCase(result) ? 1 : 0;
        userService.addStatistic(chatId, score);

        sendNextQuestion(chatId, level);
    }

    private final Map<CallbackData, Consumer<Long>> callbackHandlers = Map.of(
            CallbackData.NEXT_QUESTION_EASY, chatId -> sendNextQuestion(chatId, "easy"),
            CallbackData.NEXT_QUESTION_MEDIUM, chatId -> sendNextQuestion(chatId, "medium"),
            CallbackData.NEXT_QUESTION_HARD, chatId -> sendNextQuestion(chatId, "hard"),
            CallbackData.NEXT_QUESTION_RANDOM, chatId -> sendNextQuestion(chatId, "random"),
            CallbackData.BUTTON_PASS, this::handleChangeLevelCommand,
            CallbackData.BUTTON_FAIL, this::handleChangeLevelCommand,
            CallbackData.CHANGE_LEVEL, this::handleChangeLevelCommand,
            CallbackData.SHOW_STATISTIC, this::handleShowStatisticCommand,
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

    private void handleShowStatisticCommand(long chatId) {
        Integer statistic = userService.getStatistic(chatId);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(String.format("You statistic:  %d", statistic));
        telegramBot.send(message);
    }

    private void handleChangeLevelCommand(long chatId) {
        final Map<String, Long> counts = questionPullService.getQuestionCountsByLevel();

        final SendMessage sendMessage = service.createChangeLevelMessage(chatId, counts);

        sendMessage.setChatId(chatId);
        sendMessage.setText(changeLevel);
        telegramBot.send(sendMessage);
    }

    private void handleUnknownCommand(long chatId, String command) {
        String reply = "❓ Unknown command: " + command + "\n\nType /help to see available commands.";
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(reply);
        telegramBot.send(message);
    }
}
