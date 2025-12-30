package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Validated
@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    private static final String LIKE_PATH = "/{id}/like/{userId}";

    @Autowired
    public FilmController(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                          FilmService filmService,
                          MpaDbStorage mpaDbStorage,
                          GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable @Positive(message = "ID фильма должен быть положительным числом") Long id) {
        log.info("Получен запрос на получение фильма с ID: {}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с ID %d не найден", id)));
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        validateFilmReleaseDate(film);
        validateFilmMpa(film);
        validateFilmGenres(film);
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        if (film.getId() == null) {
            log.error("ID фильма не указан при обновлении");
            throw new ValidationException("ID должен быть указан");
        }

        if (!filmStorage.existsById(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new NotFoundException(String.format("Фильм с ID %d не найден", film.getId()));
        }

        validateFilmReleaseDate(film);
        validateFilmMpa(film);
        validateFilmGenres(film);
        return filmStorage.update(film);
    }

    @PutMapping(LIKE_PATH)
    public void addLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping(LIKE_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным числом") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным числом") Long userId) {
        log.info("Получен запрос на удаление лайка фильму {} от пользователя {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Параметр count должен быть не менее 1")
            int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validateFilmMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            log.error("Рейтинг MPA не указан для фильма");
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }

        if (!mpaDbStorage.getMpaById(film.getMpa().getId()).isPresent()) {
            log.error("MPA рейтинг с ID {} не найден", film.getMpa().getId());
            throw new NotFoundException(String.format("MPA рейтинг с ID %d не найден", film.getMpa().getId()));
        }
    }

    private void validateFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (var genre : film.getGenres()) {
                if (!genreDbStorage.getGenreById(genre.getId()).isPresent()) {
                    log.error("Жанр с ID {} не найден", genre.getId());
                    throw new NotFoundException(String.format("Жанр с ID %d не найден", genre.getId()));
                }
            }
        }
    }
}