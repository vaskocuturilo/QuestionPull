package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface QuestionPullRepository extends CrudRepository<QuestionPullEntity, UUID> {
    @Query(value = "SELECT * FROM question_pull q WHERE difficulty = :difficulty AND q.active = false ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<QuestionPullEntity> getRandomQuestion(@Param("difficulty") final String difficulty);

    @Query(value = "select * from question_pull q WHERE title = :title ORDER BY title asc ", nativeQuery = true)
    Optional<QuestionPullEntity> findByTitle(@Param("title") final String title);

    @Query(value = "select * from question_pull q WHERE body LIKE CONCAT('%', :body, '%') ORDER BY body asc ", nativeQuery = true)
    Optional<QuestionPullEntity> findByBody(@Param("body") final String body);

    @Modifying
    @Transactional
    @Query(value = "UPDATE question_pull q SET q.active = true WHERE uuid = :uuid", nativeQuery = true)
    void setActiveForQuestion(@Param("uuid") final UUID uuid);
}
