package com.example.questionpull.factory;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyboardFactoryTest {
    @Test
    void testBuilderCreatesKeyboardWithChainedMethods() {
        KeyboardFactory keyboardFactory = new KeyboardFactory();

        InlineKeyboardMarkup keyboard = keyboardFactory.builder()
                .addRow()
                .addButton("Button 1", "CALLBACK_1")
                .addButton("Button 2", "CALLBACK_2")
                .addRow()
                .addButton("Button 3", "CALLBACK_3")
                .build();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).size());
        assertEquals(1, rows.get(1).size());
        assertEquals("Button 1", rows.get(0).get(0).getText());
        assertEquals("CALLBACK_1", rows.get(0).get(0).getCallbackData());
        assertEquals("Button 3", rows.get(1).get(0).getText());
    }

    @Test
    void testEmptyRowsAreRemoved() {
        KeyboardFactory keyboardFactory = new KeyboardFactory();
        InlineKeyboardMarkup keyboard = keyboardFactory.builder()
                .addRow()
                .addButton("Button 1", "CALLBACK_1")
                .build();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        assertEquals(1, rows.size());
        assertEquals(1, rows.get(0).size());
        assertEquals("Button 1", rows.get(0).get(0).getText());
    }

    @Test
    void testMultipleRowsWithMultipleButtons() {
        KeyboardFactory keyboardFactory = new KeyboardFactory();
        InlineKeyboardMarkup keyboard = keyboardFactory.builder()
                .addRow()
                .addButton("Button 1", "CALLBACK_1")
                .addButton("Button 2", "CALLBACK_2")
                .addRow()
                .addButton("Button 3", "CALLBACK_3")
                .addButton("Button 4", "CALLBACK_4")
                .build();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).size());
        assertEquals(2, rows.get(1).size());

        assertEquals("Button 1", rows.get(0).get(0).getText());
        assertEquals("CALLBACK_4", rows.get(1).get(1).getCallbackData());
    }

    @Test
    void testBuildWithoutAddingButtons() {
        KeyboardFactory keyboardFactory = new KeyboardFactory();
        InlineKeyboardMarkup keyboard = keyboardFactory.builder().build();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();

        assertTrue(rows.isEmpty());
    }
}