package com.example.questionpull;

import com.example.questionpull.entity.DataVersionEntity;
import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.repository.DataVersionRepository;
import com.example.questionpull.repository.QuestionPullRepository;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.example.questionpull.util.Helper.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Mock
    private DataVersionRepository dataVersionRepository;

    @InjectMocks
    private StorageUtils storageUtils;

    private static final String TEST_FILENAME = "classpath:questions.json";

    private static final String VALID_JSON = """
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
            """;

    @BeforeEach
    void setUp() {
        storageUtils = new StorageUtils(
                questionPullRepository,
                objectMapper,
                resourceLoader,
                dataVersionRepository
        );
        ReflectionTestUtils.setField(storageUtils, "filename", TEST_FILENAME);
    }
    @Test
    void loadQuestionsPull_WhenChecksumMatches_ShouldSkipReload() throws Exception {
        byte[] fileBytes = VALID_JSON.getBytes();
        String checksum = computeChecksum(fileBytes);

        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));

        DataVersionEntity existingVersion = buildVersion(checksum);
        when(dataVersionRepository.findByDataKey("questions"))
                .thenReturn(Optional.of(existingVersion));

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository, never()).deleteAll();
        verify(questionPullRepository, never()).saveAll(any());
        verify(dataVersionRepository, never()).save(any());
    }

    @Test
    void loadQuestionsPull_WhenChecksumMismatch_ShouldReloadData() throws Exception {
        byte[] fileBytes = VALID_JSON.getBytes();

        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));

        DataVersionEntity staleVersion = buildVersion("old-checksum-value");
        when(dataVersionRepository.findByDataKey("questions"))
                .thenReturn(Optional.of(staleVersion));

        List<QuestionEntity> questionList = buildQuestionList();
        JavaType javaType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, QuestionEntity.class);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(byte[].class), eq(javaType))).thenReturn(questionList);

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository).deleteAll();
        verify(questionPullRepository).saveAll(questionList);
        verify(dataVersionRepository).save(any(DataVersionEntity.class));
    }

    @Test
    void loadQuestionsPull_WhenFirstLoad_ShouldSaveDataAndPersistChecksum() throws Exception {
        byte[] fileBytes = VALID_JSON.getBytes();

        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));

        when(dataVersionRepository.findByDataKey("questions")).thenReturn(Optional.empty());

        List<QuestionEntity> questionList = buildQuestionList();
        JavaType javaType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, QuestionEntity.class);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(byte[].class), eq(javaType))).thenReturn(questionList);

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository).deleteAll();
        verify(questionPullRepository).saveAll(questionList);

        ArgumentCaptor<DataVersionEntity> captor =
                ArgumentCaptor.forClass(DataVersionEntity.class);
        verify(dataVersionRepository).save(captor.capture());

        DataVersionEntity saved = captor.getValue();
        assertThat(saved.getDataKey()).isEqualTo("questions");
        assertThat(saved.getChecksum()).isEqualTo(computeChecksum(fileBytes));
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void loadQuestionsPull_WhenQuestionHasSolution_ShouldLinkSolutionToQuestion() throws Exception {
        byte[] fileBytes = VALID_JSON.getBytes();

        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));

        when(dataVersionRepository.findByDataKey("questions")).thenReturn(Optional.empty());

        List<QuestionEntity> questionList = buildQuestionList();
        JavaType javaType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, QuestionEntity.class);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(byte[].class), eq(javaType))).thenReturn(questionList);

        storageUtils.loadQuestionsPull();

        QuestionEntity question = questionList.get(0);
        assertThat(question.getSolution().getQuestionPull())
                .as("Solution must be back-linked to its parent question")
                .isSameAs(question);
    }

    @Test
    void loadQuestionsPull_WhenResourceNotFound_ShouldAbortWithoutSaving() {
        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository, never()).deleteAll();
        verify(questionPullRepository, never()).saveAll(any());
        verify(dataVersionRepository, never()).save(any());
    }

    @Test
    void loadQuestionsPull_WhenIOExceptionOccurs_ShouldLogErrorAndNotSave() throws Exception {
        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(new IOException("Disk read failed"));

        storageUtils.loadQuestionsPull();

        verify(questionPullRepository, never()).deleteAll();
        verify(questionPullRepository, never()).saveAll(any());
        verify(dataVersionRepository, never()).save(any());
    }

    @Test
    void loadQuestionsPull_WhenSaveAllFails_ShouldNotPersistChecksum() throws Exception {
        byte[] fileBytes = VALID_JSON.getBytes();

        when(resourceLoader.getResource(TEST_FILENAME)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));

        when(dataVersionRepository.findByDataKey("questions")).thenReturn(Optional.empty());

        JavaType javaType = TypeFactory.defaultInstance()
                .constructCollectionType(List.class, QuestionEntity.class);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        when(objectMapper.readValue(any(byte[].class), eq(javaType)))
                .thenReturn(buildQuestionList());
        when(questionPullRepository.saveAll(any()))
                .thenThrow(new RuntimeException("DB constraint violation"));

        storageUtils.loadQuestionsPull();

        verify(dataVersionRepository, never()).save(any());
    }
}