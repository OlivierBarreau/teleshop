package eu.telecom.teleshop.order.web;

import eu.telecom.teleshop.order.events.OrderCreated;
import eu.telecom.teleshop.order.model.Order;
import eu.telecom.teleshop.order.repository.OrderRepository;
import eu.telecom.teleshop.order.service.OrderEventPublisher;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher publisher;

    public OrderController(OrderRepository orderRepository, OrderEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order order) {
        order.setStatus("CREATED");
        Order saved = orderRepository.save(order);
        publisher.publishOrderCreated(new OrderCreated(saved.getId(), saved.getUserId(), saved.getProductId(), saved.getQuantity()));
        return ResponseEntity.created(URI.create("/orders/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        Optional<Order> o = orderRepository.findById(id);
        return o.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/open-exists")
    public ResponseEntity<OpenOrdersExistsResponse> checkOpenOrdersExist(@PathVariable Long userId) {
        boolean exists = orderRepository.existsByUserIdAndStatusIn(userId, List.of("CREATED"));
        return ResponseEntity.ok(new OpenOrdersExistsResponse(exists));
    }

    public static class OpenOrdersExistsResponse {
        private boolean exists;

        public OpenOrdersExistsResponse() {}

        public OpenOrdersExistsResponse(boolean exists) {
            this.exists = exists;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }
    }
}
