package com.example.questionpull.factory;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageFactoryTest {

    private final MessageFactory messageFactory = new MessageFactory();

    @Test
    void testCreateMessageWithKeyboard() {
        String text = "Simple Message";
        long chatId = 123456L;

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        SendMessage sendMessage = messageFactory.createMessageWithKeyboard(text, chatId, keyboard);

        assertThat(sendMessage.getText()).isEqualTo(text);
        assertThat(sendMessage.getChatId()).isEqualTo(String.valueOf(chatId));
        assertThat(sendMessage.getReplyMarkup()).isEqualTo(keyboard);
    }

    @Test
    void testCreateSimpleMessage() {
        String text = "Simple Message";
        long chatId = 123456L;

        SendMessage sendMessage = messageFactory.createSimpleMessage(text, chatId);

        assertThat(sendMessage.getText()).isEqualTo(text);
        assertThat(sendMessage.getChatId()).isEqualTo(String.valueOf(chatId));
    }
}