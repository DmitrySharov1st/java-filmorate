package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Collection<User> findAll() {
        log.info("Текущее количество пользователей: {}", users.size());
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(nextId.getAndIncrement());
        users.put(user.getId(), user);
        log.info("Пользователь создан с ID: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        User existingUser = users.get(user.getId());

        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null) {
            existingUser.setLogin(user.getLogin());
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            existingUser.setName(user.getName());
        } else if (user.getLogin() != null) {
            existingUser.setName(user.getLogin());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }

        log.info("Пользователь с ID {} обновлен", user.getId());
        return existingUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        log.info("Пользователь с ID {} удален", id);
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}