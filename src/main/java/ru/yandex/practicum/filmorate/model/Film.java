package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;

    @NotBlank(message = "Название не может быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @NotNull(message = "Дата релиза должна быть указана")
    LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма должна быть указана")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    Integer duration;

    MpaRating mpa;
    Set<Genre> genres = new LinkedHashSet<>();
    Set<Long> likes = new LinkedHashSet<>();
}