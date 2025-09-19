package eu.telecom.teleshop.inventory.messaging;

import eu.telecom.teleshop.inventory.config.RabbitConfig;
import eu.telecom.teleshop.inventory.events.OrderCreated;
import eu.telecom.teleshop.inventory.events.StockUpdated;
import eu.telecom.teleshop.inventory.model.Product;
import eu.telecom.teleshop.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryListenerTest {

    @Test
    void handleOrderCreated_updates_stock_and_publishes_event() {
        ProductRepository repo = mock(ProductRepository.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        InventoryListener listener = new InventoryListener(repo, rabbit);

        Product p = new Product("Phone", 10);
        p.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        OrderCreated event = new OrderCreated();
        event.setOrderId(100L);
        event.setProductId(1L);
        event.setQuantity(3);

        listener.handleOrderCreated(event);

        // stock should be reduced to 7 and saved
        verify(repo).save(any(Product.class));

        // published event should be stock.updated with remaining qty = 7
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> rk = ArgumentCaptor.forClass(String.class);
        verify(rabbit).convertAndSend(eq(RabbitConfig.EXCHANGE), rk.capture(), payload.capture());
        assertThat(rk.getValue()).isEqualTo("stock.updated");
        assertThat(payload.getValue()).isInstanceOf(StockUpdated.class);
        StockUpdated su = (StockUpdated) payload.getValue();
        assertThat(su.getOrderId()).isEqualTo(100L);
        assertThat(su.getProductId()).isEqualTo(1L);
        assertThat(su.getQuantity()).isEqualTo(7);
    }
}
