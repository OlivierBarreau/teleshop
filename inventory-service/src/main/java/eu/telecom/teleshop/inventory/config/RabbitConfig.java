package eu.telecom.teleshop.inventory.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "teleshop.exchange";
    public static final String ORDER_CREATED_QUEUE = "inventory.order.created";
    public static final String ORDER_FAILED_QUEUE = "inventory.order.failed";

    @Bean
    public TopicExchange teleshopExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(teleshopExchange).with("order.created");
    }

    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder.durable(ORDER_FAILED_QUEUE).build();
    }

    @Bean
    public Binding orderFailedBinding(Queue orderFailedQueue, TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderFailedQueue).to(teleshopExchange).with("order.failed");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
