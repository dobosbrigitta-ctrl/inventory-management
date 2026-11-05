package org.example.repository;

import org.example.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("TEST_ONLY_HASH");
        user.setRole("USER");

        UserEntity savedUser = userRepository.save(user);

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFindUserByUsername() {
        UserEntity user = new UserEntity();
        user.setUsername("john");
        user.setPassword("TEST_ONLY_HASH");
        user.setRole("USER");

        userRepository.save(user);

        Optional<UserEntity> foundUser = userRepository.findByUsername("john");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("john");
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {
        Optional<UserEntity> foundUser = userRepository.findByUsername("unknown");

        assertThat(foundUser).isEmpty();
    }
}