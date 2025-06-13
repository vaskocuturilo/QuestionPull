package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
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

    @Test
    void itShouldSelectQuestionByEasyLevel() {
        String level = "easy";
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level(level).build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findRandomByDifficultyExcludingIds(level, List.of(UUID.randomUUID()));

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectQuestionByMediumLevel() {
        String level = "medium";
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level(level)
                .build();

        underTest.save(question);

        Optional<QuestionPullEntity> optionalPaste = underTest.findRandomByDifficultyExcludingIds(level, List.of(UUID.randomUUID()));

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
                .example("Example")
                .level("easy")
                .build();

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
                .example("Example")
                .level("easy")
                .build();

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
        Optional<QuestionPullEntity> optionalPaste = underTest.findRandomByDifficultyExcludingIds("easy", List.of(UUID.randomUUID()));
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itShouldNotSaveQuestionWhenTitleIsNull() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title(null)
                .body("Test")
                .example("Example")
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionPullEntity.title")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenBodyIsNull() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body(null)
                .example("Example")
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionPullEntity.body")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenLevelIsNull() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level(null)
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionPullEntity.level")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenExampleIsNull() {
        QuestionPullEntity question = QuestionPullEntity
                .builder()
                .title("Test")
                .body("Test")
                .example(null)
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionPullEntity.example")
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}