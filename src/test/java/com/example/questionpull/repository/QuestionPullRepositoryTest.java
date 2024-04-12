package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@DataJpaTest(properties = {
        "spring.jpa.properties.javax.persistence.validation.mode=none"
})
class QuestionPullRepositoryTest {
    @Autowired
    QuestionPullRepository underTest;
    @PersistenceContext
    EntityManager entityManager;

    @Test
    void itShouldSelectQuestionByEasyDifficulty() {
        String level = "easy";
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty(level)
                .active(false).build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.getRandomQuestion(level);

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectQuestionByMediumDifficulty() {
        String level = "medium";
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty(level)
                .active(false).build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.getRandomQuestion(level);

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectByTitle() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty("easy")
                .active(false).build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findByTitle("Test");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectQuestionByBody() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty("easy")
                .active(false).build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findByBody("Test");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itNotShouldSelectQuestionByIdWhenIdDoesNotExist() {
        UUID uuid = UUID.randomUUID();
        Optional<QuestionPullEntity> optionalPaste = underTest.findById(uuid);
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByTitleWhenTitleDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.findByTitle("Test title");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByBodyWhenBodyDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.findByBody("Test body");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByBodyWhenDifficultyDoesNotExist() {
        Optional<QuestionPullEntity> optionalPaste = underTest.getRandomQuestion("easy");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itShouldNotSaveQuestionWhenTitleIsNull() {
        QuestionPullEntity question = new QuestionPullEntity(null, "Test body", "easy", false);

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value : com.example.questionpull.entity.QuestionPullEntity.title")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenBodyIsNull() {
        QuestionPullEntity question = new QuestionPullEntity("Test title", null, "easy", false);

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value : com.example.questionpull.entity.QuestionPullEntity.body")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenDifficultyIsNull() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty(null)
                .active(false).build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value : com.example.questionpull.entity.QuestionPullEntity.difficulty")
                .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    void itShouldBeActiveTrueAfterChange() {
        QuestionPullEntity questionBeforeChange = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty("easy")
                .active(false).build();

        QuestionPullEntity questionAfterChange = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .difficulty("easy")
                .active(true).build();

        underTest.save(questionBeforeChange);
        underTest.setActiveForQuestion(questionBeforeChange.getUuid());

        //clear cache
        entityManager.clear();

        Optional<QuestionPullEntity> optionalPaste = underTest.findByTitle("Test");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison().ignoringFieldsMatchingRegexes("uuid")
                .isEqualTo(questionAfterChange));
    }
}