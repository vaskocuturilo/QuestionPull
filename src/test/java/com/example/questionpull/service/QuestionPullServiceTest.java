package com.example.questionpull.service;

import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.factory.KeyboardFactory;
import com.example.questionpull.factory.MessageFactory;
import com.example.questionpull.service.questions.QuestionPullService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class QuestionPullServiceTest {
    @Mock
    private MessageFactory messageFactory;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private KeyboardFactory keyboardFactory;
    @Mock
    private KeyboardFactory.KeyboardBuilder keyboardBuilder;

    QuestionPullService questionPullService;

    private static final long CHAT_ID = 123456L;
    private static final String TEST_TITLE = "Test Title";
    private static final String TEST_BODY = "Test Body";
    private static final String TEST_EXAMPLE = "Test Example";

    private QuestionEntity question;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        questionPullService = new QuestionPullService(messageFactory, keyboardFactory, telegramBot);

        question = new QuestionEntity();
        question.setTitle(TEST_TITLE);
        question.setBody(TEST_BODY);
        question.setExample(TEST_EXAMPLE);
    }

    @Test
    @Disabled("Because we have an issue with questionPullService")
    void testCreateCustomMessage() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("easy", 1L);
        counts.put("medium", 1L);
        counts.put("hard", 1L);

        InlineKeyboardMarkup inlineKeyboardMarkup = mock(InlineKeyboardMarkup.class);

        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(String.valueOf(CHAT_ID));
        expectedMessage.setText("Choose an option:");
        expectedMessage.setReplyMarkup(inlineKeyboardMarkup);

        when(keyboardFactory.builder()).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().addRow()).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().addButton(anyString(), anyString())).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().build()).thenReturn(inlineKeyboardMarkup);
        when(messageFactory.createMessageWithKeyboard("Choose an option:", CHAT_ID, inlineKeyboardMarkup))
                .thenReturn(expectedMessage);

        SendMessage actualMessage = questionPullService.createCustomMessage(CHAT_ID, counts);

        assertEquals(expectedMessage, actualMessage);
        verify(keyboardBuilder, times(7)).addRow();
        verify(keyboardBuilder, times(7)).addButton(anyString(), anyString());
        verify(keyboardBuilder).build();
        verify(messageFactory).createMessageWithKeyboard("Choose an option:", CHAT_ID, inlineKeyboardMarkup);
    }

    @Test
    @Disabled("Because we have an issue with questionPullService")
    void testCreateChangeLevelMessage() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("easy", 1L);
        counts.put("medium", 1L);
        counts.put("hard", 1L);

        InlineKeyboardMarkup inlineKeyboardMarkup = mock(InlineKeyboardMarkup.class);

        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(String.valueOf(CHAT_ID));
        expectedMessage.setText("Choose an option:");
        expectedMessage.setReplyMarkup(inlineKeyboardMarkup);

        when(keyboardFactory.builder()).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().addRow()).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().addButton(anyString(), anyString())).thenReturn(keyboardBuilder);
        when(keyboardFactory.builder().build()).thenReturn(inlineKeyboardMarkup);
        when(messageFactory.createMessageWithKeyboard("Choose an option:", CHAT_ID, inlineKeyboardMarkup))
                .thenReturn(expectedMessage);

        SendMessage actualMessage = questionPullService.createChangeLevelMessage(CHAT_ID, counts);

        assertEquals(expectedMessage, actualMessage);
        verify(keyboardBuilder, times(7)).addRow();
        verify(keyboardBuilder, times(7)).addButton(anyString(), anyString());
        verify(keyboardBuilder).build();
        verify(messageFactory).createMessageWithKeyboard("Choose an option:", CHAT_ID, inlineKeyboardMarkup);
    }

    @Test
    void testSendQuestionMessage() {
        String formattedText = """
                Title: Test Title
                Body: Test Body
                Example: Test Example
                """;
        String level = "easy";
        InlineKeyboardMarkup keyboard = mock(InlineKeyboardMarkup.class);
        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(String.valueOf(CHAT_ID));
        expectedMessage.setText(formattedText);
        expectedMessage.setReplyMarkup(keyboard);

        when(keyboardFactory.builder()).thenReturn(keyboardBuilder);
        when(keyboardBuilder.addRow()).thenReturn(keyboardBuilder);
        when(keyboardBuilder.addButton(anyString(), anyString())).thenReturn(keyboardBuilder);
        when(keyboardBuilder.build()).thenReturn(keyboard);
        when(messageFactory.createMessageWithKeyboard(formattedText, CHAT_ID, keyboard)).thenReturn(expectedMessage);

        // Act
        questionPullService.sendQuestionMessage(question, CHAT_ID, level);

        // Assert
        verify(telegramBot).send(expectedMessage);


    }

    @Test
    void testFormatQuestionMessage() {
        String expectedMessage = """
                Title: Test Title
                Body: Test Body
                Example: Test Example
                """;

        String actualMessage = questionPullService.formatQuestionMessage(question);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void testSendStopMessage() {
        // Arrange
        long chatId = 12345L;
        String endMessage = "Thank you for participating!";
        SendMessage expectedMessage = new SendMessage();
        expectedMessage.setChatId(String.valueOf(chatId));
        expectedMessage.setText(endMessage);

        when(messageFactory.createSimpleMessage(endMessage, chatId)).thenReturn(expectedMessage);

        // Act
        questionPullService.sendStopMessage(chatId, endMessage);

        // Assert
        verify(telegramBot).send(expectedMessage);
    }

    @Test
    void testSendMessageToUser() {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(CHAT_ID));
        message.setText("Test Message");

        questionPullService.sendMessageToUser(message);

        verify(telegramBot).send(message);
    }
}