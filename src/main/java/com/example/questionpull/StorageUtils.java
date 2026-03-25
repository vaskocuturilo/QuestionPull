package com.example.questionpull;

import com.example.questionpull.entity.DataVersionEntity;
import com.example.questionpull.entity.QuestionEntity;
import com.example.questionpull.repository.DataVersionRepository;
import com.example.questionpull.repository.QuestionPullRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class StorageUtils {
    private final QuestionPullRepository questionPullRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final DataVersionRepository dataVersionRepository;

    @Value("${bot.fileName:classpath:questions.json}")
    private String filename;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadQuestionsPull() {
        try {
            if (questionPullRepository.count() > 0) {
                log.info("Questions are already loaded in the database.");
                return;
            }

            log.info("Cleared old questions to sync with latest JSON.");

            Resource resource = resourceLoader.getResource(filename);
            if (!resource.exists()) {
                log.error("Resource file {} not found!", filename);
                return;
            }

            byte[] fileBytes = resource.getInputStream().readAllBytes();
            String currentChecksum = computeChecksum(fileBytes);

            Optional<DataVersionEntity> versionOpt =
                    dataVersionRepository.findByDataKey("questions");

            if (versionOpt.isPresent() &&
                    versionOpt.get().getChecksum().equals(currentChecksum)) {
                log.info("Questions JSON unchanged (checksum match). Skipping reload.");
                return;
            }

            log.info("Questions JSON changed or first load detected. Reloading data...");

            questionPullRepository.deleteAll();

            TypeFactory typeFactory = objectMapper.getTypeFactory();
            List<QuestionEntity> questionList = objectMapper.readValue(fileBytes,
                    typeFactory.constructCollectionType(List.class, QuestionEntity.class));

            for (QuestionEntity question : questionList) {
                if (question.getSolution() != null) {
                    question.getSolution().setQuestionPull(question);
                }
            }

            questionPullRepository.saveAll(questionList);

            DataVersionEntity version = versionOpt.orElseGet(() -> {
                DataVersionEntity v = new DataVersionEntity();
                v.setDataKey("questions");
                return v;
            });
            version.setChecksum(currentChecksum);
            version.setUpdatedAt(LocalDateTime.now());
            dataVersionRepository.save(version);

            log.info("Questions reloaded successfully. New checksum: {}", currentChecksum);

        } catch (Exception e) {
            log.error("Failed to load questions: {}", e.getMessage(), e);
        }
    }

    private String computeChecksum(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
