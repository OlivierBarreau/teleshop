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
    public static final String QUEUE_ORDER_PAID = "shipping.order.paid";
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }
    
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(QUEUE_ORDER_PAID, true);
    }
    
    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPaidQueue).to(exchange).with("order.paid");
    }
}
