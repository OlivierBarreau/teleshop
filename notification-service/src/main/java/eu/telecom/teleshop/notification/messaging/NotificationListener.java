package eu.telecom.teleshop.notification.messaging;

import eu.telecom.teleshop.notification.events.OrderCreated;
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
}
