package com.example.questionpull;

import com.example.questionpull.config.StorageProperties;
import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class StorageUtils {
    private final QuestionPullRepository questionPullRepository;
    private final StorageProperties storageProperties;

    @Autowired
    public StorageUtils(QuestionPullRepository questionPullRepository, StorageProperties storageProperties) {
        this.questionPullRepository = questionPullRepository;
        this.storageProperties = storageProperties;
    }

    public void loadQuestionsPull() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            var loader = ResourceUtils.getFile(storageProperties.getFileName());
            List<QuestionPullEntity> questionList = objectMapper.readValue(loader,
                    typeFactory.constructCollectionType(List.class, QuestionPullEntity.class));
            questionPullRepository.saveAll(questionList);
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
