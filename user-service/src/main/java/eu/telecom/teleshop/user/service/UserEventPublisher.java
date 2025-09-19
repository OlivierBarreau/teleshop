package eu.telecom.teleshop.user.service;

import eu.telecom.teleshop.user.config.RabbitConfig;
import eu.telecom.teleshop.user.events.UserDeleted;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserDeleted(UserDeleted event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "user.deleted", event);
    }
}
