package eu.telecom.teleshop.inventory.repository;

import eu.telecom.teleshop.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
