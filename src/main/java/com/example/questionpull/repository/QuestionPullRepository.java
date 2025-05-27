package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionPullRepository extends CrudRepository<QuestionPullEntity, UUID> {

    @Query("SELECT q from QuestionPullEntity q WHERE q.title = :title ORDER BY q.title asc")
    Optional<QuestionPullEntity> findByTitle(@Param("title") final String title);

    @Query("SELECT q from QuestionPullEntity q WHERE q.body LIKE CONCAT('%', :body, '%') ORDER BY q.body asc ")
    Optional<QuestionPullEntity> findByBody(@Param("body") final String body);

    @Query("SELECT q FROM QuestionPullEntity q WHERE q.difficulty = :difficulty AND q.uuid NOT IN :excludedIds ORDER BY function('RAND') LIMIT 1")
    Optional<QuestionPullEntity> findRandomByDifficultyExcludingIds(@Param("difficulty") String difficulty, @Param("excludedIds") List<UUID> excludedIds);
}
