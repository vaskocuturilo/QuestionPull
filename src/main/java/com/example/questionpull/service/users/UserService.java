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
        user.setStatisticArray(0);
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
    public UserEntity addStatistic(Long chatId, Integer value) {
        UserEntity existUser = usersRepository.findByChatId(chatId);

        if (existUser == null) {
            throw new IllegalArgumentException("User with chatId" + chatId + "Not found");
        }

        int currentStat = Optional.ofNullable(existUser.getStatisticArray()).orElse(0);
        existUser.setStatisticArray(currentStat + value);

        return usersRepository.save(existUser);
    }

    @Override
    public Integer getStatistic(Long chatId) {
        UserEntity existUser = usersRepository.findByChatId(chatId);

        if (existUser == null) {
            throw new IllegalArgumentException("User with chatId " + chatId + " Not found");
        }
        return existUser.getStatisticArray();
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
            user.setStatisticArray(0);
        } else {
            user.setHistoryArray(new ArrayList<>());
            user.setStatisticArray(0);
        }
        usersRepository.save(user);
    }
}
