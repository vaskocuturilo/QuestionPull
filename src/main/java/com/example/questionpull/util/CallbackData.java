package com.example.questionpull.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum CallbackData {
    NEXT_QUESTION_EASY("NEXT_QUESTION_EASY"),
    NEXT_QUESTION_MEDIUM("NEXT_QUESTION_MEDIUM"),
    NEXT_QUESTION_HARD("NEXT_QUESTION_HARD"),
    COMPARE_MY_SOLUTION("COMPARE_MY_SOLUTION"),
    NEXT_QUESTION_RANDOM("NEXT_QUESTION_RANDOM"),
    CHANGE_LEVEL("CHANGE_LEVEL"),
    SHOW_STATISTIC("SHOW_STATISTIC"),
    STOP_QUESTION("STOP_QUESTION"),
    BUTTON_PASS("BUTTON_PASS"),
    BUTTON_FAIL("BUTTON_FAIL"),
    HELP("HELP");

    private final String value;

    CallbackData(String value) {
        this.value = value;
    }

    public static Optional<CallbackData> fromString(final String value) {
        return Arrays.stream(values())
                .filter(callbackData -> callbackData
                        .getValue()
                        .equalsIgnoreCase(value))
                .findFirst();
    }
}
