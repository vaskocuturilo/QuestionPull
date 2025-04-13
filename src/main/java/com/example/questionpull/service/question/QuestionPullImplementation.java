package com.example.questionpull.service.question;

import com.example.questionpull.entity.QuestionPullEntity;
import com.example.questionpull.repository.QuestionPullRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class QuestionPullImplementation implements QuestionPull {

    private final QuestionPullRepository questionPullRepository;

    public QuestionPullImplementation(QuestionPullRepository questionPullRepository) {
        this.questionPullRepository = questionPullRepository;
    }

    @Override
    public Optional<QuestionPullEntity> getRandomQuestion(String level) {
        return questionPullRepository.getRandomQuestion(level);
    }

    @Override
    public void setActiveForQuestion(UUID uuid) {
        questionPullRepository.setActiveForQuestion(uuid);
    }
}
