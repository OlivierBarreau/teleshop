package eu.telecom.teleshop.notification.messaging;

import eu.telecom.teleshop.notification.events.OrderCreated;
import eu.telecom.teleshop.notification.events.OrderPaid;
import eu.telecom.teleshop.notification.events.OrderShipped;
import eu.telecom.teleshop.notification.events.StockUpdated;
import eu.telecom.teleshop.notification.events.UserDeleted;
import eu.telecom.teleshop.notification.model.Notification;
import eu.telecom.teleshop.notification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationRepository repository;

    public NotificationListener(NotificationRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "notification.order.created")
    public void onOrderCreated(OrderCreated event) {
        String msg = String.format("Order %d created for user %d (product %d x%d)",
                event.getOrderId(), event.getUserId(), event.getProductId(), event.getQuantity());
        repository.save(new Notification("ORDER_CREATED", msg, event.getUserId()));
        System.out.println("[Notification] " + msg);
    }

    @RabbitListener(queues = "notification.stock.updated")
    public void onStockUpdated(StockUpdated event) {
        String msg = String.format("Stock updated for product %d -> qty %d (order %d)",
                event.getProductId(), event.getQuantity(), event.getOrderId());
        repository.save(new Notification("STOCK_UPDATED", msg, null));
        System.out.println("[Notification] " + msg);
    }

    @RabbitListener(queues = "notification.user.deleted")
    public void onUserDeleted(UserDeleted event) {
        long count = repository.deleteByUserId(event.getUserId());
        String msg = String.format("Deleted %d notifications for user %d", count, event.getUserId());
        System.out.println("[Notification] " + msg);
    }

    @RabbitListener(queues = "notification.order.paid")
    public void onOrderPaid(OrderPaid event) {
        String msg = String.format("Order %d paid by user %d - Payment ID: %d, Amount: €%.2f",
                event.getOrderId(), event.getUserId(), event.getPaymentId(), event.getAmount() / 100.0);
        repository.save(new Notification("ORDER_PAID", msg, event.getUserId()));
        System.out.println("[Notification] " + msg);
    }

    @RabbitListener(queues = "notification.order.shipped")
    public void onOrderShipped(OrderShipped event) {
        String msg = String.format("Order %d shipped to user %d - Tracking: %s via %s",
                event.getOrderId(), event.getUserId(), event.getTrackingNumber(), event.getCarrier());
        repository.save(new Notification("ORDER_SHIPPED", msg, event.getUserId()));
        System.out.println("[Notification] " + msg);
    }
}
