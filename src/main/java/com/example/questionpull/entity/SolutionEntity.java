package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "solutions")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SolutionEntity extends AbstractBaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "uuid")
    private QuestionPullEntity questionPull;

    @Column(name = "content", nullable = false, length = 2550000)
    private String content;
}
