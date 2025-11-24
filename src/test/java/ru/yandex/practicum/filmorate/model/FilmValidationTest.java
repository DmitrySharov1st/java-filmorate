package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {
    private static Validator validator;
    private Film validFilm;

    @BeforeAll
    static void setUpClass() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Название");
        validFilm.setDescription("Описание");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldCreateValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        validFilm.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Пустое название должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenNameIsNull() {
        validFilm.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Null название должно вызывать ошибку");
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        String longDescription = "A".repeat(201);
        validFilm.setDescription(longDescription);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Описание длиннее 200 символов должно вызывать ошибку");
    }

    @Test
    void shouldAcceptWhenDescriptionIsExactly200Characters() {
        String exactLengthDescription = "A".repeat(200);
        validFilm.setDescription(exactLengthDescription);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно быть допустимо");
    }

    @Test
    void shouldAcceptWhenDescriptionIsNull() {
        validFilm.setDescription(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Null описание должно быть допустимо");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        validFilm.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Отрицательная продолжительность должна вызывать ошибку");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertFalse(violations.isEmpty(), "Продолжительность 0 должна вызывать ошибку");
    }

    @Test
    void shouldAcceptWhenDurationIsPositive() {
        validFilm.setDuration(1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Положительная продолжительность должна быть допустима");
    }
}