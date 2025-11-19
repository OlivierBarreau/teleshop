package eu.telecom.teleshop.shipping.service;

import eu.telecom.teleshop.shipping.events.OrderShipped;
import eu.telecom.teleshop.shipping.model.Shipment;
import eu.telecom.teleshop.shipping.model.ShipmentStatus;
import eu.telecom.teleshop.shipping.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ShippingService {
    
    private final ShipmentRepository shipmentRepository;
    private final ShippingEventPublisher eventPublisher;
    
    public ShippingService(ShipmentRepository shipmentRepository, ShippingEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Create a shipment for an order
     */
    public Shipment createShipment(Long orderId, Long userId, Long paymentId) {
        // Generate tracking number
        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create shipment entity
        Shipment shipment = new Shipment(orderId, userId, paymentId, trackingNumber);
        shipment.setCarrier("Standard Carrier");
        shipment.setShippingAddress("Default Address");
        shipment.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
        
        // Save shipment
        shipment = shipmentRepository.save(shipment);
        
        return shipment;
    }
    
    /**
     * Process shipment - mark as shipped and publish event
     */
    public Shipment processShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        // Mark as shipped
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setShippedAt(LocalDateTime.now());
        shipment = shipmentRepository.save(shipment);
        
        // Publish OrderShipped event
        OrderShipped event = new OrderShipped(
            shipment.getId(),
            shipment.getOrderId(),
            shipment.getUserId(),
            shipment.getTrackingNumber(),
            shipment.getCarrier()
        );
        eventPublisher.publishOrderShipped(event);
        
        return shipment;
    }
    
    /**
     * Mark shipment as delivered
     */
    public Shipment markDelivered(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        
        return shipmentRepository.save(shipment);
    }
}
