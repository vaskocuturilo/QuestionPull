package com.example.questionpull.service.questions;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import com.example.questionpull.service.cache.QuestionCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionPullImplementation implements QuestionPull {

    private final QuestionPullRepository questionPullRepository;
    private final QuestionCacheService cacheService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public QuestionPullImplementation(QuestionPullRepository questionPullRepository, QuestionCacheService cacheService) {
        this.questionPullRepository = questionPullRepository;
        this.cacheService = cacheService;
    }

    @Override
    public Optional<QuestionPullEntity> getRandomQuestionExcludingIds(String level, List<UUID> excludedIds) {
        List<QuestionPullEntity> cachedQuestions = cacheService.getQuestionsByLevel(level);

        if (!cachedQuestions.isEmpty()) {
            List<QuestionPullEntity> available = cachedQuestions.stream()
                    .filter(q -> !excludedIds.contains(q.getUuid()))
                    .toList();

            if (!available.isEmpty()) {
                QuestionPullEntity selected = available.get(RANDOM.nextInt(available.size()));
                log.info("Question [{}] fetched from Redis for level [{}]", selected.getUuid(), level);
                return Optional.of(selected);
            }
        }

        log.info("Fetching random question from DB for level [{}] (cache empty or all questions used)", level);
        Optional<QuestionPullEntity> dbQuestion =
                questionPullRepository.findRandomByDifficultyExcludingIds(level, excludedIds);

        dbQuestion.ifPresent(question -> {
            cacheService.cacheQuestion(question);
            log.info("Question [{}] cached to Redis after DB fallback", question.getUuid());
        });

        return dbQuestion;
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
