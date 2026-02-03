package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = {"easy", "medium", "hard", "random"})
    void itShouldSelectQuestionByEasyLevel(final String level) {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level(level).build();

        underTest.save(question);

        Optional<QuestionEntity> optionalPaste = underTest.findRandomByDifficultyExcludingIds(level, List.of(UUID.randomUUID()));

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectByUUID() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level("easy")
                .build();

        underTest.save(question);

        Optional<QuestionEntity> optionalPaste = underTest.findByUuid(question.getUuid());

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectByTitle() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level("easy")
                .build();

        underTest.save(question);

        Optional<QuestionEntity> optionalPaste = underTest.findByTitle("Test");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itShouldSelectQuestionByBody() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level("easy")
                .build();

        underTest.save(question);

        Optional<QuestionEntity> optionalPaste = underTest.findByBody("Test");

        assertThat(optionalPaste).isPresent().hasValueSatisfying(c -> assertThat(c)
                .usingRecursiveComparison()
                .isEqualTo(question));
    }

    @Test
    void itNotShouldSelectQuestionByIdWhenIdDoesNotExist() {
        final UUID uuid = UUID.randomUUID();
        Optional<QuestionEntity> optionalPaste = underTest.findByUuid(uuid);
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByTitleWhenTitleDoesNotExist() {
        Optional<QuestionEntity> optionalPaste = underTest.findByTitle("Test title");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByBodyWhenBodyDoesNotExist() {
        Optional<QuestionEntity> optionalPaste = underTest.findByBody("Test body");
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itNotShouldSelectQuestionByBodyWhenDifficultyDoesNotExist() {
        Optional<QuestionEntity> optionalPaste = underTest.findRandomByDifficultyExcludingIds("easy", List.of(UUID.randomUUID()));
        assertThat(optionalPaste).isNotPresent();
    }

    @Test
    void itShouldNotSaveQuestionWhenTitleIsNull() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title(null)
                .body("Test")
                .example("Example")
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionEntity.title")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenBodyIsNull() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body(null)
                .example("Example")
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionEntity.body")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenLevelIsNull() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example("Example")
                .level(null)
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionEntity.level")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void itShouldNotSaveQuestionWhenExampleIsNull() {
        QuestionEntity question = QuestionEntity
                .builder()
                .title("Test")
                .body("Test")
                .example(null)
                .level("easy")
                .build();

        assertThatThrownBy(() -> underTest.save(question))
                .hasMessage("not-null property references a null or transient value: com.example.questionpull.entity.QuestionEntity.example")
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}