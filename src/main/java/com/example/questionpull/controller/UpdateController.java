package com.example.questionpull.controller;

import com.example.questionpull.service.QuestionPullServiceImplementation;
import com.example.questionpull.service.SomeService;
import com.example.questionpull.service.TelegramBot;
import com.example.questionpull.util.CallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class UpdateController {
    private TelegramBot telegramBot;
    private final QuestionPullServiceImplementation questionPullService;
    private final SomeService service;

    @Value("${bot.message.end.questions}")
    String endMessage;

    @Value("${bot.message.stop.questions}")
    String stopQuiz;

    public UpdateController(QuestionPullServiceImplementation questionPullService,
                            SomeService service) {
        this.questionPullService = questionPullService;
        this.service = service;
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
        questionPullService.getRandomQuestion(level).ifPresentOrElse(question -> {
            service.sendQuestionMessage(question, chatId);

            questionPullService.setActiveForQuestion(question.getUuid());
        }, () -> service.sendStopMessage(chatId, endMessage));
    }

    private void handleHelpCommand(long chatId) {
        final SendMessage sendMessage = service.createCustomMessage(chatId);
        String answer = "You can use menu";
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
            CallbackData.STOP_QUESTION, this::handleStopCommand,
            CallbackData.HELP, this::handleHelpCommand
    );

    private void handleUnhandledCallback(long chatId) {
        log.warn("Unhandled callback query for chatId: {}", chatId);
    }

    private void handleStopCommand(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(stopQuiz);
        telegramBot.send(message);
    }
}
