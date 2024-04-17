package com.example.questionpull.service;

import com.example.questionpull.entity.QuestionPullEntity;

import java.util.Optional;
import java.util.UUID;

public interface QuestionPullService {
    Optional<QuestionPullEntity> getRandomQuestion(final String level);

    void setActiveForQuestion(final UUID uuid);
}
