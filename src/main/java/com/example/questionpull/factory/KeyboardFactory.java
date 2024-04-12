package com.example.questionpull.factory;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardFactory {

    private KeyboardFactory() {
    }

    public static SendMessage addButtonAndSendMessage(String textToSend, long chatId, final String buttonText) {
        SendMessage message = new SendMessage();
        message.setText(textToSend);
        message.setChatId(chatId);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var inlinekeyboardButton = new InlineKeyboardButton();
        inlinekeyboardButton.setCallbackData(buttonText);
        inlinekeyboardButton.setText(buttonText);

        rowInline.add(inlinekeyboardButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        return message;
    }
}
