package eu.telecom.teleshop.notification.messaging;

import eu.telecom.teleshop.notification.events.OrderCreated;
import eu.telecom.teleshop.notification.events.StockUpdated;
import eu.telecom.teleshop.notification.events.UserDeleted;
import eu.telecom.teleshop.notification.model.Notification;
import eu.telecom.teleshop.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationListenerTest {

    @Test
    void onOrderCreated_saves_notification() {
        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationListener listener = new NotificationListener(repo);

        OrderCreated evt = new OrderCreated();
        evt.setOrderId(1L); evt.setUserId(2L); evt.setProductId(3L); evt.setQuantity(4);

        listener.onOrderCreated(evt);

        verify(repo, times(1)).save(any(Notification.class));
    }

    @Test
    void onStockUpdated_saves_notification() {
        NotificationRepository repo = mock(NotificationRepository.class);
        NotificationListener listener = new NotificationListener(repo);

        StockUpdated evt = new StockUpdated(10L, 5L, 8);

        listener.onStockUpdated(evt);

        verify(repo, times(1)).save(any(Notification.class));
    }

    @Test
    void onUserDeleted_deletes_by_userId() {
        NotificationRepository repo = mock(NotificationRepository.class);
        when(repo.deleteByUserId(7L)).thenReturn(3L);
        NotificationListener listener = new NotificationListener(repo);

        UserDeleted evt = new UserDeleted();
        evt.setUserId(7L);

        listener.onUserDeleted(evt);

        verify(repo, times(1)).deleteByUserId(7L);
    }
}
