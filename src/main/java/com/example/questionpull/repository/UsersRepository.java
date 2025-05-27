package com.example.questionpull.repository;

import com.example.questionpull.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Integer> {
    Boolean existsByChatId(Long chatId);

    UserEntity findByChatId(Long chatId);

    UserEntity findByName(String name);
}
