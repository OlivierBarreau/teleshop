package eu.telecom.teleshop.payment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "teleshop.exchange";
    public static final String ORDER_CREATED_QUEUE = "payment.order.created";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "payment.order.succeeded";
    public static final String PAYMENT_FAILED_QUEUE = "payment.order.failed";

    @Bean
    public TopicExchange teleshopExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }
    
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(teleshopExchange).with("order.created");
    }
}
