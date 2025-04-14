package com.example.questionpull.service.question;

import com.example.questionpull.entity.QuestionPullEntity;

import java.util.Optional;
import java.util.UUID;

public interface QuestionPull {
    Optional<QuestionPullEntity> getRandomQuestion(final String level);

    void setActiveForQuestion(final UUID uuid);
}
