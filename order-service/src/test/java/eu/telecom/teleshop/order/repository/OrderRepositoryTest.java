package eu.telecom.teleshop.order.repository;

import eu.telecom.teleshop.order.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
}
