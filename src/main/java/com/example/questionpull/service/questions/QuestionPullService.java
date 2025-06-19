package com.example.questionpull.service.questions;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.factory.KeyboardFactory;
import com.example.questionpull.factory.MessageFactory;
import com.example.questionpull.service.TelegramBot;
import com.example.questionpull.util.CallbackData;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;

@Service
public class QuestionPullService {

    private final MessageFactory messageFactory;
    private final KeyboardFactory keyboardFactory;
    private final TelegramBot telegramBot;
    private static final String BUTTON_NEXT_QUESTION_EASY = "⚡ Next Easy Question ";
    private static final String BUTTON_CHANGE_THE_LEVEL = "\uD83D\uDD04 Change the level";
    private static final String BUTTON_NEXT_QUESTION_MEDIUM = "⭐ Next Medium Question ";
    private static final String BUTTON_NEXT_QUESTION_HARD = "\uD83D\uDD25 Next Hard Question ";
    private static final String BUTTON_NEXT_QUESTION_RANDOM = "\uD83C\uDFB2 Next Random Question ";
    private static final String BUTTON_SHOW_STATISTIC = "\uD83D\uDCCA Show Statistic";
    private static final String BUTTON_STOP_QUESTION = "⛔ Stop Quiz";
    private static final String BUTTON_HELP_INFO_QUESTION = "ℹ️ Help & Info";
    private static final String NEXT_QUESTION_EASY = "NEXT_QUESTION_EASY";
    private static final String NEXT_QUESTION_MEDIUM = "NEXT_QUESTION_MEDIUM";
    private static final String NEXT_QUESTION_RANDOM = "NEXT_QUESTION_RANDOM";
    private static final String NEXT_QUESTION_HARD = "NEXT_QUESTION_HARD";
    private static final String STOP_QUESTION = "STOP_QUESTION";
    private static final String SHOW_STATISTIC = "SHOW_STATISTIC";
    private static final String HELP = "HELP";
    private static final String BUTTON_PASS = "✅ PASS";
    private static final String BUTTON_FAIL = "⛔ FAIL";

    public QuestionPullService(MessageFactory messageFactory, KeyboardFactory keyboardFactory, @Lazy TelegramBot telegramBot) {
        this.messageFactory = messageFactory;
        this.keyboardFactory = keyboardFactory;
        this.telegramBot = telegramBot;
    }

    public SendMessage createCustomMessage(final long chatId, final Map<String, Long> counts) {
        InlineKeyboardMarkup keyboard = buildMenuKeyboard(counts);

        return messageFactory.createMessageWithKeyboard("Choose an option:", chatId, keyboard);
    }

    public SendMessage createCustomMessage(final long chatId) {
        return messageFactory.createSimpleMessage("""
                
                This bot can help you for prepare the technical interview. You can select a level of question (easy, medium, and hard).
                Likewise, you can upload your questions.
                
                You can use commands:
                
                /start - This command will launch a question pull bot
                /help  - This command will launch a help section
                /stop  -  This command stop a question pull bot
                
                I see some students want some advice after struggling with coding challenges/exercises, getting blocked, and/or being demotivated.
                Don't worry, we've all been there. However, how you respond to that is what's important.
                Don't give up, you must be persistent.
                
                I just want to provide some tips:
                
                1. Positive mindset.
                2. Practice, lots of it.
                3. Active learning and learn from mistakes.
                4. Write clean code.
                5. Take your time and solve using multiple approaches.
                
                """, chatId);
    }

    public SendMessage createChangeLevelMessage(final long chatId, final Map<String, Long> counts) {
        InlineKeyboardMarkup keyboard = buildChangeLevelKeyboard(counts);

        return messageFactory.createMessageWithKeyboard("Choose an option:", chatId, keyboard);
    }

    public void sendQuestionMessage(final QuestionPullEntity question, final long chatId, String level) {
        String text = formatQuestionMessage(question);
        InlineKeyboardMarkup keyboard = buildQuestionMenuKeyboard(level);
        SendMessage sendMessage = messageFactory.createMessageWithKeyboard(text, chatId, keyboard);

        sendMessageToUser(sendMessage);
    }

    private InlineKeyboardMarkup buildMenuKeyboard(final Map<String, Long> counts) {
        long easyCount = counts.getOrDefault("easy", 0L);
        long mediumCount = counts.getOrDefault("medium", 0L);
        long hardCount = counts.getOrDefault("hard", 0L);

        return keyboardFactory
                .builder()
                .addRow().addButton(BUTTON_NEXT_QUESTION_EASY + "(" + easyCount + ")", NEXT_QUESTION_EASY)
                .addRow().addButton(BUTTON_NEXT_QUESTION_MEDIUM + "(" + mediumCount + ")", NEXT_QUESTION_MEDIUM)
                .addRow().addButton(BUTTON_NEXT_QUESTION_HARD + "(" + hardCount + ")", NEXT_QUESTION_HARD)
                .addRow().addButton(BUTTON_NEXT_QUESTION_RANDOM, NEXT_QUESTION_RANDOM)
                .addRow().addButton(BUTTON_SHOW_STATISTIC, SHOW_STATISTIC)
                .addRow().addButton(BUTTON_STOP_QUESTION, STOP_QUESTION)
                .addRow().addButton(BUTTON_HELP_INFO_QUESTION, HELP)
                .build();
    }

    private InlineKeyboardMarkup buildQuestionMenuKeyboard(String level) {
        String passCallback = "ANSWER_PASS_" + level.toUpperCase();
        String failCallback = "ANSWER_FAIL_" + level.toUpperCase();

        return keyboardFactory
                .builder()
                .addRow().addButton(BUTTON_PASS, passCallback)
                .addRow().addButton(BUTTON_FAIL, failCallback)
                .addRow().addButton(BUTTON_NEXT_QUESTION_RANDOM, NEXT_QUESTION_RANDOM)
                .addRow().addButton(BUTTON_CHANGE_THE_LEVEL, CallbackData.CHANGE_LEVEL.name())
                .addRow().addButton(BUTTON_SHOW_STATISTIC, CallbackData.SHOW_STATISTIC.name())
                .addRow().addButton(BUTTON_STOP_QUESTION, CallbackData.STOP_QUESTION.name())
                .build();
    }

    private InlineKeyboardMarkup buildChangeLevelKeyboard(final Map<String, Long> counts) {
        long easyCount = counts.getOrDefault("easy", 0L);
        long mediumCount = counts.getOrDefault("medium", 0L);
        long hardCount = counts.getOrDefault("hard", 0L);

        return keyboardFactory
                .builder()
                .addRow().addButton(BUTTON_NEXT_QUESTION_EASY + "(" + easyCount + ")", CallbackData.NEXT_QUESTION_EASY.name())
                .addRow().addButton(BUTTON_NEXT_QUESTION_MEDIUM + "(" + mediumCount + ")", CallbackData.NEXT_QUESTION_MEDIUM.name())
                .addRow().addButton(BUTTON_NEXT_QUESTION_HARD + "(" + hardCount + ")", CallbackData.NEXT_QUESTION_HARD.name())
                .addRow().addButton(BUTTON_NEXT_QUESTION_RANDOM, CallbackData.NEXT_QUESTION_RANDOM.name())
                .addRow().addButton(BUTTON_SHOW_STATISTIC, CallbackData.SHOW_STATISTIC.name())
                .addRow().addButton(BUTTON_STOP_QUESTION, CallbackData.STOP_QUESTION.name())
                .addRow().addButton(BUTTON_HELP_INFO_QUESTION, CallbackData.HELP.name())
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
