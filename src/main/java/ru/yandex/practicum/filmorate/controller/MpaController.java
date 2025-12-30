package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {

    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public MpaController(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaDbStorage.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable Long id) {
        log.info("Получен запрос на получение рейтинга MPA с ID: {}", id);
        return mpaDbStorage.getMpaById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Рейтинг MPA с ID %d не найден", id)));
    }
}