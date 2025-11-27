package eu.telecom.teleshop.inventory.messaging;

import eu.telecom.teleshop.inventory.config.RabbitConfig;
import eu.telecom.teleshop.inventory.events.OrderCreated;
import eu.telecom.teleshop.inventory.events.StockUpdated;
import eu.telecom.teleshop.inventory.model.Product;
import eu.telecom.teleshop.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryListener.class);
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryListener(ProductRepository productRepository, RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Listen to OrderCreated events
    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreated event) {
        Optional<Product> productOpt = productRepository.findById(event.getProductId());
        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            int newQty = Math.max(0, p.getQuantity() - event.getQuantity());
            p.setQuantity(newQty);
            productRepository.save(p);
            // publish StockUpdated
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "stock.updated", new StockUpdated(event.getOrderId(), p.getId(), newQty));
        }
    }

    // Listen to OrderFailed events to restore inventory
    @RabbitListener(queues = RabbitConfig.ORDER_FAILED_QUEUE)
    public void handleOrderFailed(OrderFailedEvent event) {
        log.info("Received order failed event for order {}: restoring inventory", event.getOrderId());

        try {
            Optional<Product> productOpt = productRepository.findById(event.getProductId());
            if (productOpt.isPresent()) {
                Product p = productOpt.get();
                int restoredQty = p.getQuantity() + event.getQuantity();
                p.setQuantity(restoredQty);
                productRepository.save(p);
                
                log.info("Restored {} units of product {} (new quantity: {})", 
                    event.getQuantity(), p.getId(), restoredQty);
                
                // Publish StockUpdated event to notify about restoration
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "stock.updated", 
                    new StockUpdated(event.getOrderId(), p.getId(), restoredQty));
            } else {
                log.warn("Product {} not found when restoring inventory for failed order {}", 
                    event.getProductId(), event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to restore inventory for order {}: {}", 
                event.getOrderId(), e.getMessage(), e);
        }
    }

    // Inner class to represent OrderFailed event from order-service
    public static class OrderFailedEvent {
        private Long orderId;
        private Long userId;
        private Long productId;
        private Integer quantity;
        private String reason;

        public OrderFailedEvent() {}

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
