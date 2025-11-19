package eu.telecom.teleshop.shipping.web;

import eu.telecom.teleshop.shipping.model.Shipment;
import eu.telecom.teleshop.shipping.repository.ShipmentRepository;
import eu.telecom.teleshop.shipping.service.ShippingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
    
    private final ShippingService shippingService;
    private final ShipmentRepository shipmentRepository;
    
    public ShipmentController(ShippingService shippingService, ShipmentRepository shipmentRepository) {
        this.shippingService = shippingService;
        this.shipmentRepository = shipmentRepository;
    }
    
    @GetMapping
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Shipment getShipment(@PathVariable Long id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shipment not found: " + id));
    }
    
    @GetMapping("/order/{orderId}")
    public Shipment getShipmentByOrderId(@PathVariable Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Shipment not found for order: " + orderId));
    }
    
    @PostMapping("/{id}/ship")
    public Shipment shipOrder(@PathVariable Long id) {
        return shippingService.processShipment(id);
    }
    
    @PostMapping("/{id}/deliver")
    public Shipment deliverOrder(@PathVariable Long id) {
        return shippingService.markDelivered(id);
    }
}
