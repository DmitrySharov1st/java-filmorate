package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        // Проверяем существование обоих пользователей
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        // Делаем idempotent: если уже друзья, просто ничего не делаем
        if (user.getFriends().contains(friendId)) {
            log.info("Пользователи {} и {} уже друзья", userId, friendId);
            return;
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        // Проверяем существование обоих пользователей
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден"));

        // Делаем операцию идемпотентной: если дружбы нет, просто возвращаемся
        if (!user.getFriends().contains(friendId)) {
            log.info("Пользователи {} и {} не являются друзьями, удаление не требуется", userId, friendId);
            return;
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        return user.getFriends().stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        User otherUser = userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + otherId + " не найден"));

        Set<Long> commonFriendIds = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonFriendIds.stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь с ID " + friendId + " не найден")))
                .collect(Collectors.toList());
    }
}