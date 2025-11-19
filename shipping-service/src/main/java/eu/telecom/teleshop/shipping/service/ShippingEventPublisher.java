package eu.telecom.teleshop.shipping.service;

import eu.telecom.teleshop.shipping.config.RabbitConfig;
import eu.telecom.teleshop.shipping.events.OrderShipped;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShippingEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public ShippingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderShipped(OrderShipped event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.shipped", event);
    }
}
