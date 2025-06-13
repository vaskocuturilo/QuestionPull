package com.example.questionpull.service.questions;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionPullImplementation implements QuestionPull {

    private final QuestionPullRepository questionPullRepository;

    public QuestionPullImplementation(QuestionPullRepository questionPullRepository) {
        this.questionPullRepository = questionPullRepository;
    }

    @Override
    public Optional<QuestionPullEntity> getRandomQuestionExcludingIds(String level, List<UUID> excludedIds) {
        return questionPullRepository.findRandomByDifficultyExcludingIds(level, excludedIds);
    }

    @Override
    public Map<String, Long> getQuestionCountsByLevel() {
        List<Object[]> result = questionPullRepository.getCountQuestions();
        return result.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }
}
