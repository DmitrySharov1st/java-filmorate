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

class UserValidationTest {
    private static Validator validator;
    private User validUser;

    @BeforeAll
    static void setUpClass() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("test@mail.ru");
        validUser.setLogin("validLogin");
        validUser.setName("Valid Name");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateValidUser() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        validUser.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пустой email должен вызывать ошибку");
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        validUser.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Null email должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Невалидный email должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        validUser.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Пустой логин должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenLoginIsNull() {
        validUser.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Null логин должен вызывать ошибку");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        validUser.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Логин с пробелами должен вызывать ошибку");
    }

    @Test
    void shouldAcceptWhenNameIsNull() {
        validUser.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Null имя должно быть допустимо");
    }

    @Test
    void shouldAcceptWhenNameIsBlank() {
        validUser.setName("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Пустое имя должно быть допустимо");
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty(), "Дата рождения в будущем должна вызывать ошибку");
    }

    @Test
    void shouldAcceptWhenBirthdayIsToday() {
        validUser.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Сегодняшняя дата должна быть допустима");
    }

    @Test
    void shouldAcceptWhenBirthdayIsInPast() {
        validUser.setBirthday(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Дата рождения в прошлом должна быть допустима");
    }
}