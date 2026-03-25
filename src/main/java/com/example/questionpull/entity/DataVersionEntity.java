package com.example.questionpull.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_version")
@Data
@NoArgsConstructor
public class DataVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dataKey;

    @Column(nullable = false)
    private String checksum;

    @Column
    private LocalDateTime updatedAt;
}
