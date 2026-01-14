package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldHandleEmptyUserObject() {
        User user = new User();
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("login")));
    }

    @Test
    void shouldHandleEmptyFilmObject() {
        Film film = new Film();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("releaseDate")));
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void shouldAcceptMaximumValidDescriptionLength() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно быть допустимо");
    }

    @Test
    void shouldHandleUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusYears(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "День рождения в будущем должен вызывать ошибку");
    }

    @Test
    void shouldHandleVeryOldUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1800, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Любая дата рождения в прошлом должна быть допустима");
    }

    @Test
    void shouldHandleUserWithCurrentDateBirthday() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Сегодняшняя дата рождения должна быть допустима");
    }

    @Test
    void shouldHandleFilmWithNullValues() {
        Film film = new Film();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(3, violations.size(), "Должны быть ошибки для name, releaseDate и duration");

        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("releaseDate")));
        assertTrue(violations.stream().anyMatch(v
                -> v.getPropertyPath().toString().equals("duration")));
    }

    @Test
    void shouldHandleUserWithNullValues() {
        User user = new User();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size(), "Должны быть ошибки для email и login");
    }
}