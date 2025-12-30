package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmDbStorage filmDbStorage,
                       @Qualifier("userDbStorage") UserDbStorage userDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
    }

    public void addLike(Long filmId, Long userId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }

        if (!filmDbStorage.existsById(filmId)) {
            throw new NotFoundException(String.format("Фильм с ID %d не найден", filmId));
        }

        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!userDbStorage.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь с ID %d не найден", userId));
        }

        if (!filmDbStorage.existsById(filmId)) {
            throw new NotFoundException(String.format("Фильм с ID %d не найден", filmId));
        }

        filmDbStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным");
        }
        return filmDbStorage.getPopularFilms(count);
    }

    public List<Film> getPopularFilms() {
        return getPopularFilms(10);
    }
}