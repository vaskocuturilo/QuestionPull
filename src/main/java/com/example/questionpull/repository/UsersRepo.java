package com.example.questionpull.repository;

import com.example.questionpull.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepo extends JpaRepository<User, Integer> {
    Boolean existsByChatId(Long chatId);

    User findByChatId(Long chatId);
}
