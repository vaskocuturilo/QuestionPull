package com.example.questionpull.util;

import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import com.example.questionpull.service.cache.QuestionCacheService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CachePreloader implements ApplicationRunner {

    private final QuestionPullRepository questionRepo;
    private final QuestionCacheService cacheService;

    public CachePreloader(QuestionPullRepository questionRepo, QuestionCacheService cacheService) {
        this.questionRepo = questionRepo;
        this.cacheService = cacheService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Iterable<QuestionEntity> allQuestions = questionRepo.findAll();
        allQuestions.forEach(cacheService::cacheQuestion);
    }
}
