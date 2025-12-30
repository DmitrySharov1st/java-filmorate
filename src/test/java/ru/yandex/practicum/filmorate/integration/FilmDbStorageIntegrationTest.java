package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@Sql(scripts = {"/schema.sql", "/data.sql"})
class FilmDbStorageIntegrationTest {

    private final FilmDbStorage filmDbStorage;
    private final MpaDbStorage mpaDbStorage;

    @Test
    public void testCreateAndFindFilm() {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = mpaDbStorage.getMpaById(1L).orElseThrow();
        film.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1L, "Комедия"));
        film.setGenres(genres);

        Film createdFilm = filmDbStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();

        Optional<Film> foundFilm = filmDbStorage.findById(createdFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("id", createdFilm.getId());
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Тестовый фильм");
                    assertThat(f).hasFieldOrPropertyWithValue("duration", 120);
                    assertThat(f.getMpa()).isNotNull();
                    assertThat(f.getMpa().getId()).isEqualTo(1L);
                    assertThat(f.getGenres()).hasSize(1);
                });
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Старое название");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(mpaDbStorage.getMpaById(1L).orElseThrow());

        Film createdFilm = filmDbStorage.create(film);

        createdFilm.setName("Новое название");
        createdFilm.setDuration(150);
        createdFilm.setMpa(mpaDbStorage.getMpaById(2L).orElseThrow());

        Film updatedFilm = filmDbStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Новое название");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(2L);
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Фильм 1");
        film1.setDescription("Описание 1");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(100);
        film1.setMpa(mpaDbStorage.getMpaById(1L).orElseThrow());
        filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Фильм 2");
        film2.setDescription("Описание 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(120);
        film2.setMpa(mpaDbStorage.getMpaById(2L).orElseThrow());
        filmDbStorage.create(film2);

        var films = filmDbStorage.findAll();

        assertThat(films.size()).isGreaterThanOrEqualTo(7); // 5 из data.sql + 2 созданных
    }

    @Test
    public void testDeleteFilm() {
        Film film = new Film();
        film.setName("Фильм для удаления");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(mpaDbStorage.getMpaById(1L).orElseThrow());

        Film createdFilm = filmDbStorage.create(film);
        Long filmId = createdFilm.getId();

        assertThat(filmDbStorage.existsById(filmId)).isTrue();

        filmDbStorage.delete(filmId);

        assertThat(filmDbStorage.existsById(filmId)).isFalse();
    }

    @Test
    public void testGetPopularFilms() {
        var popularFilms = filmDbStorage.getPopularFilms(3);

        assertThat(popularFilms.size()).isLessThanOrEqualTo(3);
    }

    @Test
    public void testAddAndRemoveLike() {
        Long filmId = 1L;
        Long userId = 5L; // Пользователь 5 еще не лайкал фильм 1 в data.sql

        filmDbStorage.addLike(filmId, userId);

        Optional<Film> film = filmDbStorage.findById(filmId);
        assertThat(film).isPresent();
        assertThat(film.get().getLikes()).contains(userId);

        filmDbStorage.removeLike(filmId, userId);

        film = filmDbStorage.findById(filmId);
        assertThat(film).isPresent();
        assertThat(film.get().getLikes()).doesNotContain(userId);
    }
}