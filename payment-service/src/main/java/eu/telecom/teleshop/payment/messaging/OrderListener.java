package eu.telecom.teleshop.payment.messaging;

import eu.telecom.teleshop.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "payment.order.created")
public class OrderListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);
    private final PaymentService paymentService;
    
    public OrderListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @RabbitHandler
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event: {}", event);
        
        try {
            // Calculate payment amount (€10.00 per item in cents)
            Integer amount = event.getQuantity() * 1000;
            
            // Create payment
            paymentService.createPayment(event.getOrderId(), event.getUserId(), amount);
            
            log.info("Payment created for order {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to create payment for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
    
    // Inner class to match OrderCreated event structure
    public static class OrderCreatedEvent {
        private Long orderId;
        private Long userId;
        private Long productId;
        private Integer quantity;

        public OrderCreatedEvent() {}

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        @Override
        public String toString() {
            return "OrderCreatedEvent{orderId=" + orderId + ", userId=" + userId + 
                   ", productId=" + productId + ", quantity=" + quantity + "}";
        }
    }
}
