package com.example.questionpull.service.cache;

import com.example.questionpull.entity.QuestionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class QuestionCacheService {
    private final RedisTemplate<String, QuestionEntity> redisTemplate;

    public QuestionCacheService(RedisTemplate<String, QuestionEntity> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheQuestion(QuestionEntity question) {
        String listKey = "questions:level:" + question.getLevel();
        String itemKey = "question:" + question.getUuid();

        redisTemplate.opsForList().rightPush(listKey, question);
        redisTemplate.opsForValue().set(itemKey, question);
        log.info("Cached question [{}] to Redis under list [{}]", question.getUuid(), listKey);

    }

    public List<QuestionEntity> getQuestionsByLevel(String level) {
        String listKey = "questions:level:" + level;
        List<QuestionEntity> cachedList = redisTemplate.opsForList().range(listKey, 0, -1);

        if (cachedList == null || cachedList.isEmpty()) {
            log.info("Redis cache miss for level: {}", level);
            return Collections.emptyList();
        }

        log.info("Fetched {} questions from Redis list for level: {}", cachedList.size(), level);
        return cachedList;
    }
}
