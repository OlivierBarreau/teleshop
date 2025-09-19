package eu.telecom.teleshop.inventory.messaging;

import eu.telecom.teleshop.inventory.config.RabbitConfig;
import eu.telecom.teleshop.inventory.events.OrderCreated;
import eu.telecom.teleshop.inventory.events.StockUpdated;
import eu.telecom.teleshop.inventory.model.Product;
import eu.telecom.teleshop.inventory.repository.ProductRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryListener {

    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryListener(ProductRepository productRepository, RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreated event) {
        Optional<Product> productOpt = productRepository.findById(event.getProductId());
        if (productOpt.isPresent()) {
            Product p = productOpt.get();
            int newQty = Math.max(0, p.getQuantity() - event.getQuantity());
            p.setQuantity(newQty);
            productRepository.save(p);
            // publish StockUpdated
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, "stock.updated", new StockUpdated(event.getOrderId(), p.getId(), newQty));
        }
    }
}
