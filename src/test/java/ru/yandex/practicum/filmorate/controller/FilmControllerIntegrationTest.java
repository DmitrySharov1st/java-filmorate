package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/schema.sql", "/data.sql"})
class FilmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateValidFilm() throws Exception {
        Film film = new Film();
        film.setName("Новый фильм");
        film.setDescription("Описание нового фильма");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Новый фильм"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void createFilmWithEmptyNameShouldFail() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllFilms() throws Exception {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void shouldGetFilmById() throws Exception {
        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Интерстеллар"));
    }

    @Test
    void shouldReturnNotFoundWhenFilmNotExists() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPopularFilms() throws Exception {
        mockMvc.perform(get("/films/popular?count=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldAddLike() throws Exception {
        // Используем пользователя 5, который еще не лайкал фильм 1 (в data.sql нет лайка (1,5))
        mockMvc.perform(put("/films/1/like/5"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(4)); // 3 из data.sql + 1 новый
    }

    @Test
    void shouldFailWhenAddingExistingLike() throws Exception {
        // Пытаемся добавить уже существующий лайк (1,1) из data.sql
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Лайк от пользователя 1 фильму 1 уже существует"));
    }

    @Test
    void shouldRemoveLike() throws Exception {
        // Сначала добавим лайк, которого нет в data.sql
        mockMvc.perform(put("/films/1/like/5"));  // Пользователь 5 еще не лайкал фильм 1

        // Теперь удалим этот лайк
        mockMvc.perform(delete("/films/1/like/5"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(3)); // только из data.sql
    }

    @Test
    void shouldRemoveExistingLike() throws Exception {
        // Удаляем существующий лайк (1,1) из data.sql
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(2)); // осталось 2 лайка (2 и 3)
    }

    @Test
    void updateFilmWithEarlyReleaseDateShouldFail() throws Exception {
        Film film = new Film();
        film.setId(1L);
        film.setName("Обновленное название");
        film.setDescription("Обновленное описание");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(150);

        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        film.setMpa(mpa);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetPopularFilmsWithCorrectCount() throws Exception {
        // Проверяем, что возвращается именно 3 фильма
        mockMvc.perform(get("/films/popular?count=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldGetPopularFilmsWithDefaultCount() throws Exception {
        // Проверяем, что по умолчанию возвращается 10 фильмов ,или меньше, если фильмов меньше
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5)); // всего 5 фильмов в базе
    }
}