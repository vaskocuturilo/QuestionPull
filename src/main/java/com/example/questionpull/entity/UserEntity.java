package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity extends AbstractBaseEntity {

    Long chatId;

    String name;

    @Column(name = "current_q_id")
    UUID currentQId;

    @ElementCollection(fetch = FetchType.EAGER)
    List<UUID> historyArray;

    Integer statisticArray;
}
