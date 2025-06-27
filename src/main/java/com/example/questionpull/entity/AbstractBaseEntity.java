package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@MappedSuperclass
@Access(AccessType.FIELD)
@Data
public abstract class AbstractBaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "uuid", updatable = false, nullable = false)
    private UUID uuid;
}
