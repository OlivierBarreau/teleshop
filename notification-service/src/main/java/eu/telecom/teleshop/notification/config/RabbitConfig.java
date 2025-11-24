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
    public static final String ORDER_PAID_QUEUE = "notification.order.paid";
    public static final String ORDER_SHIPPED_QUEUE = "notification.order.shipped";
    public static final String PAYMENT_CREATED_QUEUE = "notification.payment.created";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "notification.payment.succeeded";
    public static final String PAYMENT_FAILED_QUEUE = "notification.payment.failed";

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
    public Queue orderPaidQueue() {
        return QueueBuilder.durable(ORDER_PAID_QUEUE).build();
    }

    @Bean
    public Queue orderShippedQueue() {
        return QueueBuilder.durable(ORDER_SHIPPED_QUEUE).build();
    }

    @Bean
    public Queue paymentCreatedQueue() {
        return QueueBuilder.durable(PAYMENT_CREATED_QUEUE).build();
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return QueueBuilder.durable(PAYMENT_SUCCEEDED_QUEUE).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
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
    public Binding orderPaidBinding(@Qualifier("orderPaidQueue") Queue orderPaidQueue,
                                     TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderPaidQueue)
                .to(teleshopExchange)
                .with("order.paid");
    }

    @Bean
    public Binding orderShippedBinding(@Qualifier("orderShippedQueue") Queue orderShippedQueue,
                                        TopicExchange teleshopExchange) {
        return BindingBuilder.bind(orderShippedQueue)
                .to(teleshopExchange)
                .with("order.shipped");
    }

    @Bean
    public Binding paymentCreatedBinding(@Qualifier("paymentCreatedQueue") Queue paymentCreatedQueue,
                                        TopicExchange teleshopExchange) {
        return BindingBuilder.bind(paymentCreatedQueue)
                .to(teleshopExchange)
                .with("payment.created");
    }

    @Bean
    public Binding paymentSucceededBinding(@Qualifier("paymentSucceededQueue") Queue paymentSucceededQueue,
                                          TopicExchange teleshopExchange) {
        return BindingBuilder.bind(paymentSucceededQueue)
                .to(teleshopExchange)
                .with("payment.succeeded");
    }

    @Bean
    public Binding paymentFailedBinding(@Qualifier("paymentFailedQueue") Queue paymentFailedQueue,
                                       TopicExchange teleshopExchange) {
        return BindingBuilder.bind(paymentFailedQueue)
                .to(teleshopExchange)
                .with("payment.failed");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
