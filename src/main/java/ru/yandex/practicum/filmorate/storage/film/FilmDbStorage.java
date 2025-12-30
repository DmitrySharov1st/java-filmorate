package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Primary
@Slf4j
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private final RowMapper<Film> filmRowMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         GenreDbStorage genreDbStorage,
                         MpaDbStorage mpaDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDbStorage = genreDbStorage;
        this.mpaDbStorage = mpaDbStorage;

        this.filmRowMapper = (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));

            java.sql.Date releaseDate = rs.getDate("release_date");
            if (releaseDate != null) {
                film.setReleaseDate(releaseDate.toLocalDate());
            }

            film.setDuration(rs.getInt("duration"));

            Long mpaId = rs.getLong("mpa_rating_id");
            if (!rs.wasNull()) {
                film.setMpa(mpaDbStorage.getMpaById(mpaId).orElse(null));
            }

            return film;
        };
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT * FROM film ORDER BY id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(genreDbStorage.getGenresByFilmId(film.getId())));
            film.setLikes(new HashSet<>(getLikesByFilmId(film.getId())));
        });

        return films;
    }

    @Override
    public Film create(Film film) {
        // Проверка существования MPA (дополнительная защита)
        if (!mpaDbStorage.getMpaById(film.getMpa().getId()).isPresent()) {
            throw new NotFoundException(String.format("MPA рейтинг с ID %d не найден", film.getMpa().getId()));
        }

        // Проверка существования жанров (дополнительная защита)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenreById(genre.getId()).isPresent()) {
                    throw new NotFoundException(String.format("Жанр с ID %d не найден", genre.getId()));
                }
            }
        }

        String sql = "INSERT INTO film (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(filmId, film.getGenres());
        }

        log.info("Фильм создан с ID: {}", filmId);
        return findById(filmId).orElseThrow(() ->
                new NotFoundException(String.format("Фильм с ID %d не найден после создания", filmId))
        );
    }

    @Override
    public Film update(Film film) {
        // Проверка существования MPA (дополнительная защита)
        if (!mpaDbStorage.getMpaById(film.getMpa().getId()).isPresent()) {
            throw new NotFoundException(String.format("MPA рейтинг с ID %d не найден", film.getMpa().getId()));
        }

        // Проверка существования жанров (дополнительная защита)
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenreById(genre.getId()).isPresent()) {
                    throw new NotFoundException(String.format("Жанр с ID %d не найден", genre.getId()));
                }
            }
        }

        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating_id = ? WHERE id = ?";

        int updatedRows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updatedRows == 0) {
            throw new NotFoundException(String.format("Фильм с ID %d не найден для обновления", film.getId()));
        }

        updateFilmGenres(film.getId(), film.getGenres());

        log.info("Фильм с ID {} обновлен", film.getId());
        return findById(film.getId()).orElseThrow(() ->
                new NotFoundException(String.format("Фильм с ID %d не найден после обновления", film.getId()))
        );
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM film WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                film.setGenres(new LinkedHashSet<>(genreDbStorage.getGenresByFilmId(id)));
                film.setLikes(new HashSet<>(getLikesByFilmId(id)));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM film WHERE id = ?";
        int deletedRows = jdbcTemplate.update(sql, id);
        if (deletedRows == 0) {
            throw new NotFoundException(String.format("Фильм с ID %d не найден для удаления", id));
        }
        log.info("Фильм с ID {} удален", id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM film WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Genre genre : genres) {
            batchArgs.add(new Object[]{filmId, genre.getId()});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }
    }

    private List<Long> getLikesByFilmId(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
    }

    public void addLike(Long filmId, Long userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            throw new ValidationException(String.format(
                    "Лайк от пользователя %d фильму %d уже существует", userId, filmId));
        }
    }

    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deletedRows = jdbcTemplate.update(sql, filmId, userId);
        if (deletedRows == 0) {
            log.warn("Попытка удалить несуществующий лайк: пользователь {}, фильм {}", userId, filmId);
        } else {
            log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count " +
                "FROM film f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = filmRowMapper.mapRow(rs, rowNum);
            film.setGenres(new LinkedHashSet<>(genreDbStorage.getGenresByFilmId(film.getId())));
            film.setLikes(new HashSet<>(getLikesByFilmId(film.getId())));
            return film;
        }, count);
    }
}