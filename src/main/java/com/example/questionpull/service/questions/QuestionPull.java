package com.example.questionpull.service.questions;

import com.example.questionpull.entity.QuestionPullEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionPull {
    Optional<QuestionPullEntity> getRandomQuestionExcludingIds(String level, List<UUID> history);
}
