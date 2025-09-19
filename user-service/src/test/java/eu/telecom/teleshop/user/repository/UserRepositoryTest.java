package eu.telecom.teleshop.user.repository;

import eu.telecom.teleshop.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFind() {
        User u = new User();
        u.setName("Alice");
        u.setEmail("alice@example.com");
        u.setPassword("secret");
        User saved = userRepository.save(u);
        assertThat(saved.getId()).isNotNull();

        User found = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }
}
