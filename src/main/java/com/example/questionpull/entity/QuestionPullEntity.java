package com.example.questionpull.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_pull")
public class QuestionPullEntity extends AbstractBaseEntity {

    @Column(nullable = false, length = 2550000)
    private String title;

    @Column(nullable = false, length = 2550000)
    private String body;
}
