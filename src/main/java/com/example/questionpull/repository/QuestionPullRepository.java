package com.example.questionpull.repository;

import com.example.questionpull.entity.QuestionPullEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionPullRepository extends CrudRepository<QuestionPullEntity, Integer> {

    @Query(value = "select * from question_pull q WHERE title = :title ORDER BY title asc ", nativeQuery = true)
    Optional<QuestionPullEntity> findByTitle(@Param("title") final String title);

    @Query(value = "select * from question_pull q WHERE body LIKE CONCAT('%', :body, '%') ORDER BY body asc ", nativeQuery = true)
    Optional<QuestionPullEntity> findByBody(@Param("body") final String body);
}
