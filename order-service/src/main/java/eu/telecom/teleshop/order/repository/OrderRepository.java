package eu.telecom.teleshop.order.repository;

import eu.telecom.teleshop.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
