package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.Data;

@MappedSuperclass
@Access(AccessType.FIELD)
@Data
public abstract class AbstractBaseEntity {
    public static final int START_SEQ = 100000;

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1, initialValue = START_SEQ)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    protected Integer id;
}
