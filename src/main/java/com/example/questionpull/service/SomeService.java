package com.example.questionpull.service;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.factory.KeyboardFactory;
import com.example.questionpull.factory.MessageFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class SomeService {

    private final MessageFactory messageFactory;
    private final KeyboardFactory keyboardFactory;
    private final TelegramBot telegramBot;

    public SomeService(MessageFactory messageFactory, KeyboardFactory keyboardFactory, @Lazy TelegramBot telegramBot) {
        this.messageFactory = messageFactory;
        this.keyboardFactory = keyboardFactory;
        this.telegramBot = telegramBot;
    }

    public SendMessage createCustomMessage(final long chatId) {
        InlineKeyboardMarkup keyboard = keyboardFactory
                .builder()
                .addRow()
                .addButton("➡️ Next Question", "NEXT_QUESTION")
                .addButton("⛔ Stop Quiz", "STOP_QUESTION")
                .addRow()
                .addButton("ℹ️ Help & Info", "HELP")
                .build();

        return messageFactory.createMessageWithKeyboard("Choose an option:", chatId, keyboard);
    }

    public void sendQuestionMessage(QuestionPullEntity question, long chatId) {
        String text = """
                Title: %s
                Body: %s
                Example: %s
                """.formatted(question.getTitle(), question.getBody(), question.getExample());

        InlineKeyboardMarkup keyboard = keyboardFactory
                .builder()
                .addRow()
                .addButton("➡️ Next Question", "NEXT_QUESTION")
                .addButton("⛔ Stop Quiz", "STOP_QUESTION")
                .addRow()
                .addButton("ℹ️ Help & Info", "HELP")
                .build();

        SendMessage message = messageFactory.createMessageWithKeyboard(text, chatId, keyboard);

        telegramBot.send(message);
    }

    public void sendStopMessage(long chatId, String endMessage) {
        SendMessage message = messageFactory.createSimpleMessage(endMessage, chatId);
        telegramBot.send(message);
    }
}
