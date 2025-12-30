-- Удаление существующих таблиц (в обратном порядке из-за внешних ключей)
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS film;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS mpa_ratings;

-- Создание таблицы рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa_ratings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);

-- Создание таблицы жанров
CREATE TABLE IF NOT EXISTS genres (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_login_no_spaces CHECK (login NOT LIKE '% %')
);

-- Создание таблицы фильмов
CREATE TABLE IF NOT EXISTS film (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    mpa_rating_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_film_mpa_rating FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings(id),
    CONSTRAINT chk_release_date CHECK (release_date >= '1895-12-28'),
    CONSTRAINT chk_duration_positive CHECK (duration > 0)
);

-- Создание таблицы дружбы
CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_status_values CHECK (status IN ('CONFIRMED')), -- Убрали 'PENDING'
    CONSTRAINT chk_not_self_friend CHECK (user_id != friend_id)
);

-- Создание таблицы лайков
CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_likes_film FOREIGN KEY (film_id) REFERENCES film(id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Создание таблицы связи фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genres_film FOREIGN KEY (film_id) REFERENCES film(id) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);