package eu.telecom.teleshop.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "teleshop.exchange";
    public static final String ORDER_CREATED_QUEUE = "notification.order.created";
    public static final String STOCK_UPDATED_QUEUE = "notification.stock.updated";
    public static final String USER_DELETED_QUEUE = "notification.user.deleted";

    @Bean
    public TopicExchange teleshopExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue stockUpdatedQueue() {
        return QueueBuilder.durable(STOCK_UPDATED_QUEUE).build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding(@Qualifier("orderCreatedQueue") Queue orderCreatedQueue,
                                       TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(teleshopExchange)
                .with("order.created");
    }

    @Bean
    public Binding stockUpdatedBinding(@Qualifier("stockUpdatedQueue") Queue stockUpdatedQueue,
                                       TopicExchange teleshopExchange) {
        return BindingBuilder.bind(stockUpdatedQueue)
                .to(teleshopExchange)
                .with("stock.updated");
    }

    @Bean
    public Binding userDeletedBinding(@Qualifier("userDeletedQueue") Queue userDeletedQueue,
                                      TopicExchange teleshopExchange) {
        return BindingBuilder.bind(userDeletedQueue)
                .to(teleshopExchange)
                .with("user.deleted");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
