package com.example.questionpull.service;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.factory.KeyboardFactory;
import com.example.questionpull.factory.MessageFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class QuestionPullService {
    private final MessageFactory messageFactory;
    private final KeyboardFactory keyboardFactory;
    private final TelegramBot telegramBot;
    private static final String BUTTON_NEXT_QUESTION_EASY = "⚡ Next Question Easy";
    private static final String BUTTON_NEXT_QUESTION_MEDIUM = "⭐ Next Question Medium";
    private static final String BUTTON_NEXT_QUESTION_HARD = "\uD83D\uDD25 Next Question Hard";
    private static final String BUTTON_STOP_QUESTION = "⛔ Stop Quiz";
    private static final String BUTTON_HELP_INFO_QUESTION = "ℹ️ Help & Info";
    private static final String NEXT_QUESTION_EASY = "NEXT_QUESTION_EASY";
    private static final String NEXT_QUESTION_MEDIUM = "NEXT_QUESTION_MEDIUM";
    private static final String NEXT_QUESTION_HARD = "NEXT_QUESTION_HARD";
    private static final String STOP_QUESTION = "STOP_QUESTION";
    private static final String HELP = "HELP";

    public QuestionPullService(MessageFactory messageFactory, KeyboardFactory keyboardFactory, @Lazy TelegramBot telegramBot) {
        this.messageFactory = messageFactory;
        this.keyboardFactory = keyboardFactory;
        this.telegramBot = telegramBot;
    }

    public SendMessage createCustomMessage(final long chatId) {
        InlineKeyboardMarkup keyboard = buildMenuKeyboard();

        return messageFactory.createMessageWithKeyboard("Choose an option:", chatId, keyboard);
    }

    public void sendQuestionMessage(final QuestionPullEntity question, final long chatId) {
        String text = formatQuestionMessage(question);
        InlineKeyboardMarkup keyboard = buildMenuKeyboard();
        SendMessage sendMessage = messageFactory.createMessageWithKeyboard(text, chatId, keyboard);

        sendMessageToUser(sendMessage);
    }

    private InlineKeyboardMarkup buildMenuKeyboard() {
        return keyboardFactory
                .builder()
                .addRow()
                .addButton(BUTTON_NEXT_QUESTION_EASY, NEXT_QUESTION_EASY)
                .addButton(BUTTON_NEXT_QUESTION_MEDIUM, NEXT_QUESTION_MEDIUM)
                .addButton(BUTTON_NEXT_QUESTION_HARD, NEXT_QUESTION_HARD)
                .addButton(BUTTON_STOP_QUESTION, STOP_QUESTION)
                .addRow()
                .addButton(BUTTON_HELP_INFO_QUESTION, HELP)
                .build();
    }

    public String formatQuestionMessage(QuestionPullEntity question) {
        return """
                Title: %s
                Body: %s
                Example: %s
                """.formatted(question.getTitle(), question.getBody(), question.getExample());
    }

    public void sendStopMessage(long chatId, String endMessage) {
        SendMessage message = messageFactory.createSimpleMessage(endMessage, chatId);
        telegramBot.send(message);
    }

    public void sendMessageToUser(final SendMessage message) {
        telegramBot.send(message);
    }
}
