package com.example.questionpull;

import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.entity.SolutionEntity;
import com.example.questionpull.repository.QuestionPullRepository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageUtilsTest {

    @Mock
    private QuestionPullRepository questionPullRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private StorageUtils storageUtils;

    private final String testFilename = "classpath:questions.json";

    @BeforeEach
    void setUp() {
        storageUtils = new StorageUtils(
                questionPullRepository,
                objectMapper,
                resourceLoader,
                testFilename
        );
    }

    @Test
    void testLoadQuestionsPull_WhenDataAlreadyExists_ShouldNotLoad() throws IOException {
        when(questionPullRepository.count()).thenReturn(5L);

        storageUtils.loadQuestionsPull();

        verify(resourceLoader, never()).getResource(anyString());
        verify(objectMapper, never()).readValue(any(InputStream.class), any(JavaType.class));
        verify(questionPullRepository, never()).saveAll(any());
    }

    @Test
    void testLoadQuestionsPull_WhenResourceNotFound_ShouldLogError() {
        when(questionPullRepository.count()).thenReturn(0L);
        when(resourceLoader.getResource(testFilename)).thenReturn(resource);
        when((resource).exists()).thenReturn(false);

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository, never()).saveAll(any());
    }

    @Test
    void testLoadQuestionsPull_WhenValidJson_ShouldSaveData() throws Exception {
        when(questionPullRepository.count()).thenReturn(0L);
        when(resourceLoader.getResource(testFilename)).thenReturn(resource);
        when((resource).exists()).thenReturn(true);

        InputStream mockInputStream = new ByteArrayInputStream("""
                    [
                      {
                        "title": "Title1",
                        "body": "Body1",
                        "example": "Example1",
                        "level": "easy",
                        "solution": {
                          "content": "Use streams..."
                        }
                      }
                    ]
                """.getBytes());

        when(resource.getInputStream()).thenReturn(mockInputStream);

        List<QuestionEntity> questionList = new ArrayList<>();
        QuestionEntity question = new QuestionEntity();
        question.setTitle("Title1");
        question.setBody("Body1");
        question.setExample("Example1");
        question.setLevel("easy");

        SolutionEntity solution = new SolutionEntity();
        solution.setContent("Use streams...");
        question.setSolution(solution);

        questionList.add(question);

        JavaType javaType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, QuestionEntity.class);

        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(InputStream.class), eq(javaType))).thenReturn(questionList);

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository).saveAll(questionList);
        assertSame(question, solution.getQuestionPull(), "Solution should be linked to Question");
    }

    @Test
    void testLoadQuestionsPull_WhenExceptionOccurs_ShouldLogError() throws Exception {
        when(questionPullRepository.count()).thenReturn(0L);
        when(resourceLoader.getResource(testFilename)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(new IOException("File read failed"));

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository, never()).saveAll(any());
    }
}