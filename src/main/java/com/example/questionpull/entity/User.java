package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Long chatId;

    String name;
    @Column(name = "current_q_id")
    Integer currentQId;

    @ElementCollection(fetch = FetchType.EAGER)
    List<Integer> historyArray;
}
