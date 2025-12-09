package eu.telecom.teleshop.order.repository;

import eu.telecom.teleshop.order.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void saveAndGet() {
        Order o = new Order();
        o.setUserId(1L);
        o.setProductId(1L);
        o.setQuantity(2);
        o.setStatus("CREATED");
        Order saved = repository.save(o);
        assertThat(saved.getId()).isNotNull();

        Order found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo("CREATED");
    }

    @Test
    void existsByUserIdAndStatusIn_returnsTrue_whenOpenOrderExists() {
        // Given
        Order order = new Order();
        order.setUserId(100L);
        order.setProductId(1L);
        order.setQuantity(1);
        order.setStatus("CREATED");
        repository.save(order);

        // When
        boolean exists = repository.existsByUserIdAndStatusIn(100L, List.of("CREATED"));

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndStatusIn_returnsFalse_whenNoOpenOrderExists() {
        // Given
        Order order = new Order();
        order.setUserId(200L);
        order.setProductId(1L);
        order.setQuantity(1);
        order.setStatus("COMPLETED");
        repository.save(order);

        // When
        boolean exists = repository.existsByUserIdAndStatusIn(200L, List.of("CREATED"));

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserIdAndStatusIn_returnsFalse_whenUserHasNoOrders() {
        // When
        boolean exists = repository.existsByUserIdAndStatusIn(999L, List.of("CREATED"));

        // Then
        assertThat(exists).isFalse();
    }
}
