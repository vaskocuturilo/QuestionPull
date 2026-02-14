package com.example.questionpull.controller;

import com.example.questionpull.entity.SolutionEntity;
import com.example.questionpull.entity.UserEntity;
import com.example.questionpull.service.TelegramBot;
import com.example.questionpull.service.questions.QuestionPullImplementation;
import com.example.questionpull.service.questions.QuestionPullService;
import com.example.questionpull.service.users.UserService;
import com.example.questionpull.util.CallbackData;
import jakarta.persistence.EntityNotFoundException;
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

    @Value("${bot.message.help.info}")
    String showHelpInfo;

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
                log.info("User started bot: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
                handleStartCommand(chatId, update.getMessage().getChat().getFirstName());
            }
            case "/help" -> {
                log.info("User use the help command: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
                handleHelpCommand(chatId);
            }
            case "/stop" -> {
                log.info("User use the stop command: chatId={}, name={}", chatId, update.getMessage().getChat().getFirstName());
                handleStopCommand(chatId);
            }
            default -> handleUnknownCommand(chatId, messageText);
        }
    }

    private void sendNextQuestion(final long chatId, String level) {
        UserEntity user = userService.findOrCreateUser(chatId, "");
        List<UUID> history = user.getHistoryArray() != null ? user.getHistoryArray() : new ArrayList<>();

        String finalLevel = level.contains("random") ? levels.get(RANDOM.nextInt(0, levels.size())) : level;

        questionPullService.getRandomQuestionExcludingIds(finalLevel, history)
                .ifPresentOrElse(question -> {
                    history.add(question.getUuid());
                    user.setCurrentQId(question.getUuid());
                    user.setHistoryArray(history);
                    userService.updateUser(user);

                    service.sendQuestionMessage(question, chatId, finalLevel);
                }, () -> service.sendStopMessage(chatId, endMessage));
    }

    private void handleHelpCommand(long chatId) {
        final SendMessage sendMessage = service.createCustomMessage(chatId, showHelpInfo);
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
        Map<CallbackData, Consumer<Long>> tempMap = new EnumMap<>(CallbackData.class);

        tempMap.put(CallbackData.NEXT_QUESTION_EASY, chatId -> sendNextQuestion(chatId, "easy"));
        tempMap.put(CallbackData.NEXT_QUESTION_MEDIUM, chatId -> sendNextQuestion(chatId, "medium"));
        tempMap.put(CallbackData.NEXT_QUESTION_HARD, chatId -> sendNextQuestion(chatId, "hard"));
        tempMap.put(CallbackData.NEXT_QUESTION_RANDOM, chatId -> sendNextQuestion(chatId, "random"));
        tempMap.put(CallbackData.BUTTON_PASS, this::handleChangeLevelCommand);
        tempMap.put(CallbackData.BUTTON_FAIL, this::handleChangeLevelCommand);
        tempMap.put(CallbackData.COMPARE_MY_SOLUTION, this::handleCompareWithMySolutionCallback);
        tempMap.put(CallbackData.CHECK_BIG_O, this::handleCheckBigO);
        tempMap.put(CallbackData.CHANGE_LEVEL, this::handleChangeLevelCommand);
        tempMap.put(CallbackData.SHOW_STATISTIC, this::handleShowStatisticCommand);
        tempMap.put(CallbackData.STOP_QUESTION, this::handleStopCommand);
        tempMap.put(CallbackData.HELP, this::handleHelpCommand);

        String callBackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (callBackData.startsWith("ANSWER_PASS_") || callBackData.startsWith("ANSWER_FAIL_")) {
            handleAnswerCallback(callBackData, chatId);
            return;
        }

        CallbackData.fromString(callBackData)
                .ifPresentOrElse(callback -> Map.copyOf(tempMap)
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
        final String reply = "❓ Unknown command: " + command + "\n\nType /help to see available commands.";
        sendText(chatId, reply);
    }

    private void handleCheckBigO(long chatId) {
        final String reply = "❌ This functionality is not available yet";
        sendText(chatId, reply);
    }

    private void handleCompareWithMySolutionCallback(long chatId) {
        log.info("Compare-with-solution requested for chatId={}", chatId);

        final String withoutSolution = "Haven't solution for this question yet";

        log.info("Use compare with my solution functionality for chatId: {}", chatId);

        final var existedUser = userService
                .getUserByChatId(chatId)
                .orElseThrow(() -> new EntityNotFoundException("User not found for chatId = " + chatId));

        final var currentQuestionId = existedUser.getCurrentQId();

        if (currentQuestionId == null) {
            sendText(chatId, withoutSolution);
            return;
        }

        final var question = questionPullService
                .getQuestionById(currentQuestionId)
                .orElseThrow(() -> new EntityNotFoundException("The question not found: " + currentQuestionId));

        final SolutionEntity solution = question.getSolution();

        if (Objects.isNull(solution) || Objects.isNull(solution.getContent()) || solution.getContent().isEmpty()) {
            log.info("No solution found for question [{}]", question.getUuid());
            sendText(chatId, withoutSolution);
            return;
        }
        log.info("Sending solution for question [{}]", question.getUuid());

        sendText(chatId, solution.getContent());
    }

    private void sendText(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        telegramBot.send(message);
    }
}
