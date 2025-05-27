package com.example.questionpull.service;

import com.example.questionpull.entity.UserEntity;
import com.example.questionpull.repository.UsersRepository;
import com.example.questionpull.service.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findOrCreateUser_shouldReturnExistingUser() {
        Long chatId = 123L;
        String name = "Alice";
        UserEntity existingUser = new UserEntity();
        existingUser.setChatId(chatId);
        existingUser.setName(name);

        when(usersRepository.existsByChatId(chatId)).thenReturn(true);
        when(usersRepository.findByChatId(chatId)).thenReturn(existingUser);

        UserEntity result = userService.findOrCreateUser(chatId, name);

        assertEquals(existingUser, result);
        verify(usersRepository).findByChatId(chatId);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void findOrCreateUser_shouldCreateNewUser() {
        Long chatId = 456L;
        String name = "Bob";
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        when(usersRepository.existsByChatId(chatId)).thenReturn(false);
        when(usersRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.findOrCreateUser(chatId, name);

        verify(usersRepository).save(captor.capture());
        UserEntity savedUser = captor.getValue();

        assertEquals(chatId, savedUser.getChatId());
        assertEquals(name, savedUser.getName());
        assertNotNull(savedUser.getCurrentQId());
        assertNotNull(savedUser.getHistoryArray());
    }

    @Test
    void updateUser_shouldSaveUser() {
        UserEntity user = new UserEntity();
        user.setChatId(789L);

        userService.updateUser(user);

        verify(usersRepository).save(user);
    }

    @Test
    void getUserByChatId_shouldReturnUserWhenExists() {
        Long chatId = 101L;
        UserEntity user = new UserEntity();
        user.setChatId(chatId);

        when(usersRepository.findByChatId(chatId)).thenReturn(user);

        Optional<UserEntity> result = userService.getUserByChatId(chatId);

        assertTrue(result.isPresent());
        assertEquals(chatId, result.get().getChatId());
    }

    @Test
    void getUserByChatId_shouldReturnEmptyWhenNotFound() {
        Long chatId = 202L;
        when(usersRepository.findByChatId(chatId)).thenReturn(null);

        Optional<UserEntity> result = userService.getUserByChatId(chatId);

        assertTrue(result.isEmpty());
    }

    @Test
    void addStatistic_shouldUpdateUserStatistic() {
        Long chatId = 303L;
        String name = "StatUser";
        int statValue = 5;
        UserEntity user = new UserEntity();
        user.setChatId(chatId);

        when(usersRepository.findByChatId(chatId)).thenReturn(user);
        when(usersRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = userService.addStatistic(chatId, name, statValue);

        assertEquals(user, result);
        assertEquals(statValue, result.getStatisticArray());
    }

    @Test
    void resetUserQuestions_shouldClearHistory() {
        Long chatId = 404L;
        UserEntity user = new UserEntity();
        user.setChatId(chatId);
        user.setHistoryArray(new ArrayList<>(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())));

        when(usersRepository.findByChatId(chatId)).thenReturn(user);

        userService.resetUserQuestions(chatId);

        assertTrue(user.getHistoryArray().isEmpty());
        verify(usersRepository).save(user);
    }
}
