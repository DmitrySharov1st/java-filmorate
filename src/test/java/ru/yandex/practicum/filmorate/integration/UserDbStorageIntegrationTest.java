package ru.yandex.practicum.filmorate.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class UserDbStorageIntegrationTest {

    private final UserDbStorage userDbStorage;

    @Test
    public void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();

        Optional<User> foundUser = userDbStorage.findById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", createdUser.getId());
                    assertThat(u).hasFieldOrPropertyWithValue("email", "test@example.com");
                    assertThat(u).hasFieldOrPropertyWithValue("login", "testlogin");
                    assertThat(u).hasFieldOrPropertyWithValue("name", "Test User");
                });
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldlogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.create(user);

        createdUser.setEmail("new@example.com");
        createdUser.setLogin("newlogin");
        createdUser.setName("New Name");
        createdUser.setBirthday(LocalDate.of(1995, 1, 1));

        User updatedUser = userDbStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1995, 1, 1));
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("testuser1@example.com"); // Было: "user1@example.com"
        user1.setLogin("testuser1");
        user1.setName("Test User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userDbStorage.create(user1);

        User user2 = new User();
        user2.setEmail("testuser2@example.com"); // Было: "user2@example.com"
        user2.setLogin("testuser2");
        user2.setName("Test User Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        userDbStorage.create(user2);

        var users = userDbStorage.findAll();

        assertThat(users.size()).isGreaterThanOrEqualTo(7); // 5 из data.sql + 2 созданных
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setEmail("delete@example.com");
        user.setLogin("deletelogin");
        user.setName("Delete User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.create(user);
        Long userId = createdUser.getId();

        assertThat(userDbStorage.existsById(userId)).isTrue();

        userDbStorage.delete(userId);

        assertThat(userDbStorage.existsById(userId)).isFalse();
    }

    @Test
    public void testAddAndGetFriends() {
        User user1 = new User();
        user1.setEmail("friend1@example.com");
        user1.setLogin("friend1");
        user1.setName("Friend One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userDbStorage.create(user1);

        User user2 = new User();
        user2.setEmail("friend2@example.com");
        user2.setLogin("friend2");
        user2.setName("Friend Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User createdUser2 = userDbStorage.create(user2);

        userDbStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        userDbStorage.confirmFriend(createdUser2.getId(), createdUser1.getId());

        var friends = userDbStorage.getFriends(createdUser1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(createdUser2.getId());
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("common1@example.com");
        user1.setLogin("common1");
        user1.setName("Common One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userDbStorage.create(user1);

        User user2 = new User();
        user2.setEmail("common2@example.com");
        user2.setLogin("common2");
        user2.setName("Common Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User createdUser2 = userDbStorage.create(user2);

        User user3 = new User();
        user3.setEmail("common3@example.com");
        user3.setLogin("common3");
        user3.setName("Common Three");
        user3.setBirthday(LocalDate.of(2000, 1, 1));
        User createdUser3 = userDbStorage.create(user3);

        userDbStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        userDbStorage.confirmFriend(createdUser3.getId(), createdUser1.getId());

        userDbStorage.addFriend(createdUser2.getId(), createdUser3.getId());
        userDbStorage.confirmFriend(createdUser3.getId(), createdUser2.getId());

        var commonFriends = userDbStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(createdUser3.getId());
    }

    @Test
    public void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("remove1@example.com");
        user1.setLogin("remove1");
        user1.setName("Remove One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userDbStorage.create(user1);

        User user2 = new User();
        user2.setEmail("remove2@example.com");
        user2.setLogin("remove2");
        user2.setName("Remove Two");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User createdUser2 = userDbStorage.create(user2);

        userDbStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userDbStorage.confirmFriend(createdUser2.getId(), createdUser1.getId());

        var friendsBefore = userDbStorage.getFriends(createdUser1.getId());
        assertThat(friendsBefore).hasSize(1);

        userDbStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        var friendsAfter = userDbStorage.getFriends(createdUser1.getId());
        assertThat(friendsAfter).hasSize(0);
    }
}