package com.example.questionpull.service.cache;

import com.example.questionpull.entity.QuestionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionCacheServiceTest {

    @Mock
    private RedisTemplate<String, QuestionEntity> redisTemplate;

    @Mock
    private ListOperations<String, QuestionEntity> listOps;

    @Mock
    private ValueOperations<String, QuestionEntity> valueOps;

    private QuestionCacheService cacheService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(redisTemplate.opsForList()).thenReturn(listOps);
        Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        cacheService = new QuestionCacheService(redisTemplate);
    }

    @Test
    void shouldCacheQuestionCorrectly() {
        // Arrange
        QuestionEntity question = new QuestionEntity();
        question.setUuid(UUID.randomUUID());
        question.setLevel("easy");

        // Act
        cacheService.cacheQuestion(question);

        // Assert
        String listKey = "questions:level:easy";
        String itemKey = "question:" + question.getUuid();

        verify(listOps).rightPush(listKey, question);
        verify(valueOps).set(itemKey, question);
    }

    @Test
    void shouldReturnQuestionsFromCache() {
        // Arrange
        QuestionEntity question1 = new QuestionEntity();
        question1.setUuid(UUID.randomUUID());
        question1.setLevel("easy");

        List<QuestionEntity> mockList = List.of(question1);

        when(listOps.range("questions:level:easy", 0, -1)).thenReturn(mockList);

        // Act
        List<QuestionEntity> result = cacheService.getQuestionsByLevel("easy");

        // Assert
        assertEquals(1, result.size());
        assertEquals(question1.getUuid(), result.get(0).getUuid());
    }

    @Test
    void shouldReturnEmptyListWhenCacheIsEmpty() {
        // Arrange
        when(listOps.range("questions:level:medium", 0, -1)).thenReturn(null);

        // Act
        List<QuestionEntity> result = cacheService.getQuestionsByLevel("medium");

        // Assert
        assertTrue(result.isEmpty());
    }
}