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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotProperties config;

    private final QuestionPullRepository questionPullRepository;

    private final StorageUtils storageUtils;

    private static final String NEXT_QUESTION = "NEXT_QUESTION";

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

                    checkPull(question, chatId);

                    question.ifPresentOrElse(questionPullEntity -> addButtonAndSendMessage(
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
        } else if (update.hasCallbackQuery()) {

            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(NEXT_QUESTION)) {

                var question = getQuestionFromPull();

                checkPull(question, chatId);

                question.ifPresent(questionPullEntity -> addButtonAndEditText(String.format("""
                                 Title: %s,
                                Question: %s
                                 """, questionPullEntity.getTitle(),
                        questionPullEntity.getBody()), chatId, update.getCallbackQuery().getMessage().getMessageId()));
            }
        }
    }

    private void checkPull(Optional<QuestionPullEntity> question, long chatId) {
        if (question.isEmpty()) {
            sendMessage("Your question pull is empty", chatId);
            return;
        }
        questionPullRepository.setActiveForQuestion(question.get().getId());
    }

    private Optional<QuestionPullEntity> getQuestionFromPull() {
        return questionPullRepository.getRandomQuestion("easy");
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

    private void addButtonAndSendMessage(String textToSend, long chatId) {

        SendMessage message = new SendMessage();
        message.setText(textToSend);
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(NEXT_QUESTION);
        inlinekeyboardButton.setText("next question");
        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        send(message);
    }

    private void addButtonAndEditText(String joke, long chatId, Integer messageId) {

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(joke);
        message.setMessageId(messageId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(NEXT_QUESTION);
        inlinekeyboardButton.setText("next question");
        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        sendEditMessageText(message);
    }

    private void sendEditMessageText(EditMessageText msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
