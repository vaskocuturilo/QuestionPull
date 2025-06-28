package com.example.questionpull.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
@Builder
public class QuestionEntity extends AbstractBaseEntity {

    @Column(nullable = false, length = 2550000)
    private String title;

    @Column(nullable = false, length = 2550000)
    private String body;

    @Column(nullable = false, length = 2550000)
    private String example;

    @Column(nullable = false, length = 10)
    private String level;

    @OneToOne(mappedBy = "questionPull", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private SolutionEntity solution;
}
