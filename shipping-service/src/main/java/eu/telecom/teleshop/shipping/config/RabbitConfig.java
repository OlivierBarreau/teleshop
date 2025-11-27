package eu.telecom.teleshop.shipping.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    
    public static final String EXCHANGE = "teleshop.exchange";
    public static final String ORDER_PAID_QUEUE = "shipping.order.paid";
    public static final String ORDER_FAILED_QUEUE = "shipping.order.failed";
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }
    
    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPaidQueue).to(exchange).with("order.paid");
    }
    
    @Bean
    public Queue orderFailedQueue() {
        return new Queue(ORDER_FAILED_QUEUE, true);
    }
    
    @Bean
    public Binding orderFailedBinding(Queue orderFailedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderFailedQueue).to(exchange).with("order.failed");
    }
}
