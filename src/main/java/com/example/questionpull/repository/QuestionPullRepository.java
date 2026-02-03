package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionPullRepository extends CrudRepository<QuestionEntity, UUID> {

    @Query("SELECT q from QuestionEntity q WHERE q.uuid = :uuid ORDER BY q.uuid asc")
    Optional<QuestionEntity> findByUuid(@Param("uuid") final UUID uuid);

    @Query("SELECT q from QuestionEntity q WHERE q.title = :title ORDER BY q.title asc")
    Optional<QuestionEntity> findByTitle(@Param("title") final String title);

    @Query("SELECT q from QuestionEntity q WHERE q.body LIKE CONCAT('%', :body, '%') ORDER BY q.body asc ")
    Optional<QuestionEntity> findByBody(@Param("body") final String body);

    @Query("SELECT q FROM QuestionEntity q WHERE q.level = :level AND q.uuid NOT IN :excludedIds ORDER BY RANDOM() LIMIT 1")
    Optional<QuestionEntity> findRandomByDifficultyExcludingIds(@Param("level") String level, @Param("excludedIds") List<UUID> excludedIds);

    @Query("SELECT q.level, COUNT(q) FROM QuestionEntity q GROUP BY q.level")
    List<Object[]> getCountQuestions();
}
