package com.example.questionpull;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Slf4j
public class StorageUtils {
    private final QuestionPullRepository questionPullRepository;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String filename;

    public StorageUtils(QuestionPullRepository questionPullRepository,
                        ObjectMapper objectMapper,
                        ResourceLoader resourceLoader, @Value("${bot.fileName}") String filename) {
        this.questionPullRepository = questionPullRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.filename = filename;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadQuestionsPull() {
        try {
            if (questionPullRepository.count() > 0) {
                log.info("Questions are already loaded in the database.");
                return;
            }

            File file = resourceLoader.getResource(filename).getFile();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            List<QuestionPullEntity> questionList = objectMapper.readValue(file, typeFactory.constructCollectionType(List.class, QuestionPullEntity.class));
            questionPullRepository.saveAll(questionList);
            log.info("Questions loaded successfully into the database.");
        } catch (Exception e) {
            log.error("Failed to load questions: {}", e.getMessage());
        }
    }
}
