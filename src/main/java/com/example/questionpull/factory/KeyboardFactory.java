package com.example.questionpull.factory;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public InlineKeyboardMarkup createInlineKeyboard(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public KeyboardBuilder builder() {
        return new KeyboardBuilder();
    }

    public class KeyboardBuilder {
        final List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        public KeyboardBuilder addRow() {
            rows.add(new ArrayList<>());
            return this;
        }

        public KeyboardBuilder addButton(final String text, final String callbackData) {
            if (rows.isEmpty()) {
                addRow();
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(text);
            button.setCallbackData(callbackData);
            rows.get(rows.size() - 1).add(button);

            return this;
        }

        public KeyboardBuilder addLinkButton(final String text, final String url) {
            if (rows.isEmpty()) {
                addRow();
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(text);
            button.setUrl(url);
            rows.get(rows.size() - 1).add(button);

            return this;
        }

        public InlineKeyboardMarkup build() {
            return createInlineKeyboard(rows);
        }
    }
}
