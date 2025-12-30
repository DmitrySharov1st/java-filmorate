package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserDbStorage userDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userDbStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        userDbStorage.addFriend(userId, friendId);
    }

    public void confirmFriend(Long userId, Long friendId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userDbStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        userDbStorage.confirmFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userDbStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        userDbStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return userDbStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userDbStorage.existsById(otherId)) {
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден");
        }

        return userDbStorage.getCommonFriends(userId, otherId);
    }
}