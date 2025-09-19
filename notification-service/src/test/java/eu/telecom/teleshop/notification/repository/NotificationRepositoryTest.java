package eu.telecom.teleshop.notification.repository;

import eu.telecom.teleshop.notification.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository repository;

    @Test
    void deleteByUserId_removesOnlyMatching() {
        repository.save(new Notification("ORDER_CREATED", "msg1", 1L));
        repository.save(new Notification("ORDER_CREATED", "msg2", 2L));
        repository.save(new Notification("STOCK_UPDATED", "msg3", 1L));

        long deleted = repository.deleteByUserId(1L);
        assertThat(deleted).isEqualTo(2);
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findAll().get(0).getUserId()).isEqualTo(2L);
    }
}
