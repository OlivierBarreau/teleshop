package eu.telecom.teleshop.shipping.messaging;

import eu.telecom.teleshop.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "shipping.order.paid")
public class OrderPaidListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderPaidListener.class);
    private final ShippingService shippingService;
    
    public OrderPaidListener(ShippingService shippingService) {
        this.shippingService = shippingService;
    }
    
    @RabbitHandler
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Received order paid event: {}", event);
        
        try {
            // Create shipment for the paid order
            shippingService.createShipment(event.getOrderId(), event.getUserId(), event.getPaymentId());
            
            log.info("Shipment created for order {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to create shipment for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
    
    // Inner class to match OrderPaid event structure
    public static class OrderPaidEvent {
        private Long orderId;
        private Long userId;
        private Long paymentId;
        private Integer amount;

        public OrderPaidEvent() {}

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
        
        @Override
        public String toString() {
            return "OrderPaidEvent{orderId=" + orderId + ", userId=" + userId + 
                   ", paymentId=" + paymentId + ", amount=" + amount + "}";
        }
    }
}
