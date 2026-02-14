package com.example.questionpull;

import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.entity.SolutionEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
public class StorageUtils {
    private final QuestionPullRepository questionPullRepository;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${bot.fileName:classpath:questions.json}")
    private final String filename;

    public StorageUtils(QuestionPullRepository questionPullRepository,
                        ObjectMapper objectMapper,
                        ResourceLoader resourceLoader, @Value("${BOT_FILE_NAME:classpath:questions.json}") String filename) {
        this.questionPullRepository = questionPullRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.filename = filename;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadQuestionsPull() {
        try {
            if (questionPullRepository.count() > 0) {
                log.info("Questions are already loaded in the database.");
                return;
            }

            questionPullRepository.deleteAll();
            log.info("Cleared old questions to sync with latest JSON.");

            Resource resource = resourceLoader.getResource(filename);
            if (!resource.exists()) {
                log.error("Resource file {} not found!", filename);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                List<QuestionEntity> questionList = objectMapper.readValue(inputStream,
                        typeFactory.constructCollectionType(List.class, QuestionEntity.class));

                for (QuestionEntity question : questionList) {
                    if (question.getSolution() != null) {
                        SolutionEntity solution = question.getSolution();
                        solution.setQuestionPull(question);
                    }
                }
                questionPullRepository.saveAll(questionList);
                log.info("Questions loaded successfully into the database.");
            }
        } catch (Exception e) {
            log.error("Failed to load questions: {}", e.getMessage(), e);
        }
    }
}
