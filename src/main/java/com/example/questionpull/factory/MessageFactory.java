package com.example.questionpull.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class MessageFactory {
    public SendMessage createMessageWithKeyboard(final String text, final long chatId, final InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    public SendMessage createSimpleMessage(final String text, final long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        return sendMessage;
    }
}
