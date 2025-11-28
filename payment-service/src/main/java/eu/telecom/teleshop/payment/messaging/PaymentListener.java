package eu.telecom.teleshop.payment.messaging;

import eu.telecom.teleshop.payment.events.OrderCreated;
import eu.telecom.teleshop.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import eu.telecom.teleshop.payment.config.RabbitConfig;

@Component
public class PaymentListener {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentListener.class);
    private final PaymentService paymentService;
    
    public PaymentListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreated event) {
        log.info("Received order created event: {}", event);
        
        if (event == null) {
            log.warn("Received null order created event");
            return;
        }
        
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
}
