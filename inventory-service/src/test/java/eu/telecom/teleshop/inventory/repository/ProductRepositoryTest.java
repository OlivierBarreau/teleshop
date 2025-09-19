package eu.telecom.teleshop.inventory.repository;

import eu.telecom.teleshop.inventory.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void saveAndFind() {
        Product p = new Product("Test", 7);
        Product saved = repository.save(p);
        assertThat(saved.getId()).isNotNull();

        Product found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getQuantity()).isEqualTo(7);
    }
}
