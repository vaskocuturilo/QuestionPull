package com.example.questionpull.service.users;

import com.example.questionpull.entity.UserEntity;

import java.util.Optional;

public interface User {
    UserEntity findOrCreateUser(Long chatId, String name);
    void updateUser(UserEntity user);
    Optional<UserEntity> getUserByChatId(Long chatId);
    UserEntity addStatistic(Long chatId, String name, Integer value);
    void resetUserQuestions(Long chatId);

}
