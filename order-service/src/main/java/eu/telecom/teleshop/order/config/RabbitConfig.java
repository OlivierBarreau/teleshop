package eu.telecom.teleshop.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "teleshop.exchange";
    public static final String PAYMENT_FAILED_QUEUE = "order.payment.failed";

    @Bean
    public TopicExchange teleshopExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange teleshopExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(teleshopExchange).with("payment.failed");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
