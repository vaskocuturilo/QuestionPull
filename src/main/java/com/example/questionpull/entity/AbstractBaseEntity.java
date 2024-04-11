package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@MappedSuperclass
@Access(AccessType.FIELD)
@Data
public abstract class AbstractBaseEntity {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    protected UUID uuid;
}
