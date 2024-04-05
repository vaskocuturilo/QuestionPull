package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@DataJpaTest(properties = {
        "spring.jpa.properties.javax.persistence.validation.mode=none"
})
class QuestionPullRepositoryTest {
    @Autowired
    QuestionPullRepository underTest;

    @Test
    void itShouldSelectQuestionById() {
        QuestionPullEntity question = new QuestionPullEntity(1, "Test1", "Test1");

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findById(1);

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectByQuestion() {
        QuestionPullEntity question = new QuestionPullEntity(2, "Test2", "Test2");

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findByTitle("Test2");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectQuestionByBody() {
        QuestionPullEntity question = new QuestionPullEntity(3, "Test3", "Test3");

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findByBody("Test3");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itNotShouldSelectQuestionByIdWhenIdDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.findById(100);
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByTitleWhenTitleDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.findByTitle("TEST TITLE");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByBodyWhenBodyDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.findByBody("TEST BODY");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itShouldNotSaveQuestionWhenIdIsNull() {
        QuestionPullEntity question = new QuestionPullEntity(null, "TEST TITLE", "TEST BODY");

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("Identifier of entity 'com.example.questionpull.entity.QuestionPullEntity' must be manually assigned before calling 'persist()'")
                .isInstanceOf(JpaSystemException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenTitleIsNull() {
        QuestionPullEntity question = new QuestionPullEntity(1, null, "TEST BODY");

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value : com.example.questionpull.entity.QuestionPullEntity.title")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenBodyIsNull() {
        QuestionPullEntity question = new QuestionPullEntity(1, "TEST TITLE", null);

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value : com.example.questionpull.entity.QuestionPullEntity.body")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

}