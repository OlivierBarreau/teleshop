package eu.telecom.teleshop.order.messaging;

import eu.telecom.teleshop.order.config.RabbitConfig;
import eu.telecom.teleshop.order.events.OrderFailed;
import eu.telecom.teleshop.order.model.Order;
import eu.telecom.teleshop.order.repository.OrderRepository;
import eu.telecom.teleshop.order.service.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderListener(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment failed event for order {}: {}", event.getOrderId(), event.getReason());

        try {
            Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                
                // Update order status to FAILED
                order.setStatus("FAILED");
                orderRepository.save(order);
                log.info("Order {} marked as FAILED", order.getId());

                // Publish OrderFailed event to trigger inventory restoration and shipment cancellation
                OrderFailed orderFailedEvent = new OrderFailed(
                    order.getId(),
                    order.getUserId(),
                    order.getProductId(),
                    order.getQuantity(),
                    "Payment failed: " + event.getReason()
                );
                eventPublisher.publishOrderFailed(orderFailedEvent);
                log.info("Published order.failed event for order {}", order.getId());
            } else {
                log.warn("Order {} not found when processing payment failure", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to handle payment failure for order {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }

    // Inner class to represent PaymentFailed event from payment-service
    public static class PaymentFailedEvent {
        private Long paymentId;
        private Long orderId;
        private Long userId;
        private Integer amount;
        private String reason;

        public PaymentFailedEvent() {}

        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
