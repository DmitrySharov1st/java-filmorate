package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        log.error("Ошибка валидации: {}", errorMessage);
        errors.put("error", errorMessage);
        return errors;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException ex) {
        Map<String, String> errors = new HashMap<>();
        log.error("Ошибка валидации: {}", ex.getMessage());
        errors.put("error", ex.getMessage());
        return errors;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        log.error("Объект не найден: {}", ex.getMessage());
        errors.put("error", ex.getMessage());
        return errors;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errors = new HashMap<>();
        String errorMessage = String.format("Параметр '%s' должен быть числом", ex.getName());
        log.error("Ошибка типа параметра: {}", errorMessage);
        errors.put("error", errorMessage);
        return errors;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMissingParams(MissingServletRequestParameterException ex) {
        Map<String, String> errors = new HashMap<>();
        String errorMessage = String.format("Отсутствует обязательный параметр: %s", ex.getParameterName());
        log.error("Отсутствует параметр: {}", errorMessage);
        errors.put("error", errorMessage);
        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidFormatException(HttpMessageNotReadableException ex) {
        Map<String, String> errors = new HashMap<>();
        log.error("Некорректный формат JSON: {}", ex.getMessage());

        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("LocalDate")) {
                errors.put("error", "Неверный формат даты. Используйте формат: ГГГГ-ММ-ДД");
            } else if (message.contains("Integer") || message.contains("int")) {
                errors.put("error", "Неверный формат числа");
            } else {
                errors.put("error", "Некорректный формат данных в запросе");
            }
        } else {
            errors.put("error", "Некорректный формат данных в запросе");
        }
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        errors.put("error", "Внутренняя ошибка сервера");
        return errors;
    }
}