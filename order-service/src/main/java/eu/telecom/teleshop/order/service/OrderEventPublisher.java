package eu.telecom.teleshop.order.service;

import eu.telecom.teleshop.order.config.RabbitConfig;
import eu.telecom.teleshop.order.events.OrderCreated;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreated(OrderCreated event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "order.created", event);
    }
}
