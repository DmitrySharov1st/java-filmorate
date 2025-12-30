package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/schema.sql", "/data.sql"})
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateValidUser() throws Exception {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.login").value("testlogin"));
    }

    @Test
    void createUserWithBadEmailShouldFail() throws Exception {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"));
    }

    @Test
    void shouldAddFriend() throws Exception {
        // Используем пользователя 1 и 5
        mockMvc.perform(put("/users/1/friends/5"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // Было 2, стало 3: старые друзья (2, 3) + новый друг (5)
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRemoveFriend() throws Exception {
        // Добавляем нового друга (пользователя 5)
        mockMvc.perform(put("/users/1/friends/5"))
                .andExpect(status().isOk());

        // Удаляем его
        mockMvc.perform(delete("/users/1/friends/5"))
                .andExpect(status().isNoContent());

        // Проверяем, что остались только старые друзья из data.sql
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                // Было 1, стало 2: пользователи 2 и 3 из data.sql
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() throws Exception {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testlogin"));
    }
}