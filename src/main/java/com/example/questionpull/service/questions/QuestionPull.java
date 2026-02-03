package com.example.questionpull.service.questions;

import com.example.questionpull.entity.QuestionEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface QuestionPull {
    Optional<QuestionEntity> getRandomQuestionExcludingIds(String level, List<UUID> history);

    Optional<QuestionEntity> getQuestionById(UUID uuid);

    Map<String, Long> getQuestionCountsByLevel();
}
