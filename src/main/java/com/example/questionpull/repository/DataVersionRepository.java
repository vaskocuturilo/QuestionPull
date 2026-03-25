package com.example.questionpull.repository;

import com.example.questionpull.entity.DataVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataVersionRepository extends JpaRepository<DataVersionEntity, Long> {
    Optional<DataVersionEntity> findByDataKey(String dataKey);
}
