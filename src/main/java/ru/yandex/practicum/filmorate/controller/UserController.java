package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей");
        return userStorage.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id) {
        log.info("Получен запрос на получение пользователя с ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);
        if (user.getId() == null) {
            log.error("ID пользователя не указан при обновлении");
            throw new ValidationException("ID должен быть указан");
        }

        if (!userStorage.existsById(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", user.getId()));
        }

        return userStorage.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным числом") Long friendId) {

        if (id.equals(friendId)) {
            log.error("Пользователь {} пытается добавить себя в друзья", id);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        log.info("Получен запрос на добавление в друзья: пользователь {} добавляет {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void removeFriend(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID друга должен быть положительным числом") Long friendId) {
        log.info("Получен запрос на удаление из друзей: пользователь {} удаляет {}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id) {
        log.info("Получен запрос на получение друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID другого пользователя должен быть положительным числом") Long otherId) {
        log.info("Получен запрос на получение общих друзей пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}