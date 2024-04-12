package com.example.questionpull.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "question_pull")
@Builder
public class QuestionPullEntity extends AbstractBaseEntity {

    @Column(nullable = false, length = 2550000)
    private String title;

    @Column(nullable = false, length = 2550000)
    private String body;

    @Column(nullable = false, length = 10)
    private String difficulty;

    @Column(nullable = false)
    private boolean active = false;
}
