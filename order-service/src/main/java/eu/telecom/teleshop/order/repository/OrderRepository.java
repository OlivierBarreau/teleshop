package eu.telecom.teleshop.order.repository;

import eu.telecom.teleshop.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByUserIdAndStatusIn(Long userId, Collection<String> statuses);
}
