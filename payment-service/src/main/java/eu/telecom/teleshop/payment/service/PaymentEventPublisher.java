package eu.telecom.teleshop.payment.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import eu.telecom.teleshop.payment.config.RabbitConfig;
import eu.telecom.teleshop.payment.events.PaymentCreated;
import eu.telecom.teleshop.payment.events.PaymentFailed;
import eu.telecom.teleshop.payment.events.PaymentSucceeded;

@Service
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCreated(PaymentCreated event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "payment.created", event);
    }
    
    public void publishPaymentSucceeded(PaymentSucceeded event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "payment.succeeded", event);
    }
    
    public void publishPaymentFailed(PaymentFailed event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "payment.failed", event);
    }
}
