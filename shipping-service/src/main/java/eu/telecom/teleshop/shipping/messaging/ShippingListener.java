package eu.telecom.teleshop.shipping.messaging;

import eu.telecom.teleshop.shipping.model.Shipment;
import eu.telecom.teleshop.shipping.model.ShipmentStatus;
import eu.telecom.teleshop.shipping.repository.ShipmentRepository;
import eu.telecom.teleshop.shipping.service.ShippingService;
import eu.telecom.teleshop.shipping.config.RabbitConfig;
import eu.telecom.teleshop.shipping.events.OrderFailed;
import eu.telecom.teleshop.shipping.events.OrderPaid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShippingListener {
    
    private static final Logger log = LoggerFactory.getLogger(ShippingListener.class);
    private final ShippingService shippingService;
    private final ShipmentRepository shipmentRepository;
    
    public ShippingListener(ShippingService shippingService, ShipmentRepository shipmentRepository) {
        this.shippingService = shippingService;
        this.shipmentRepository = shipmentRepository;
    }
    
    @RabbitListener(queues = RabbitConfig.ORDER_PAID_QUEUE)
    @RabbitHandler
    public void handleOrderPaid(OrderPaid event) {
        log.info("Received order paid event: {}", event);
        
        try {
            // Create shipment for the paid order
            shippingService.createShipment(event.getOrderId(), event.getUserId(), event.getPaymentId());
            
            log.info("Shipment created for order {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to create shipment for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
    
    @RabbitListener(queues = "shipping.order.failed")
    @RabbitHandler
    public void handleOrderFailed(OrderFailed event) {
        log.info("Received order failed event: {}", event);
        
        try {
            // Find shipment by orderId
            Optional<Shipment> shipmentOpt = shipmentRepository.findByOrderId(event.getOrderId());
            
            if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                
                // Only cancel if shipment is not already shipped or delivered
                if (shipment.getStatus() == ShipmentStatus.PENDING || 
                    shipment.getStatus() == ShipmentStatus.PROCESSING) {
                    
                    shipment.setStatus(ShipmentStatus.CANCELLED);
                    shipmentRepository.save(shipment);
                    
                    log.info("Cancelled shipment for order {}", event.getOrderId());
                } else {
                    log.warn("Cannot cancel shipment for order {} - status is {}", 
                        event.getOrderId(), shipment.getStatus());
                }
            } else {
                log.info("No shipment found for order {} - nothing to cancel", event.getOrderId());
            }
            
        } catch (Exception e) {
            log.error("Failed to handle order failed for order {}: {}", 
                event.getOrderId(), e.getMessage());
        }
    }
    
}
