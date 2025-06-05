package com.example.questionpull.service.users;

import com.example.questionpull.entity.UserEntity;
import com.example.questionpull.repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService implements User {

    private final UsersRepository usersRepository;

    @Override
    public UserEntity findOrCreateUser(Long chatId, String name) {
        if (Boolean.TRUE.equals(usersRepository.existsByChatId(chatId))) {
            return usersRepository.findByChatId(chatId);
        }

        UserEntity user = new UserEntity();
        user.setChatId(chatId);
        user.setName(name);
        user.setCurrentQId(UUID.randomUUID());
        user.setHistoryArray(new ArrayList<>());

        return usersRepository.save(user);
    }

    @Override
    public void updateUser(UserEntity user) {
        usersRepository.save(user);
    }

    @Override
    public Optional<UserEntity> getUserByChatId(Long chatId) {
        return Optional.ofNullable(usersRepository.findByChatId(chatId));
    }

    @Override
    public UserEntity addStatistic(Long chatId, String name, Integer value) {
        UserEntity existUser = usersRepository.findByChatId(chatId);
        existUser.setStatisticArray(value);
        return usersRepository.save(existUser);
    }

    @Override
    public void resetUserQuestions(Long chatId) {
        UserEntity user = usersRepository.findByChatId(chatId);

        if (user == null) {
            user = new UserEntity();
            user.setChatId(chatId);
            user.setName("Unknown");
            user.setCurrentQId(UUID.randomUUID());
            user.setHistoryArray(new ArrayList<>());
        } else {
            user.setHistoryArray(new ArrayList<>());
        }
        usersRepository.save(user);
    }
}
