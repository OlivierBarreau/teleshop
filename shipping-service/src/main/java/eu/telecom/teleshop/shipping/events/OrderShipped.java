package eu.telecom.teleshop.shipping.events;

public class OrderShipped {
    private Long shipmentId;
    private Long orderId;
    private Long userId;
    private String trackingNumber;
    private String carrier;

    public OrderShipped() {}
    
    public OrderShipped(Long shipmentId, Long orderId, Long userId, String trackingNumber, String carrier) {
        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.userId = userId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
    }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    @Override
    public String toString() {
        return "OrderShipped{shipmentId=" + shipmentId + ", orderId=" + orderId + 
               ", userId=" + userId + ", trackingNumber='" + trackingNumber + "', carrier='" + carrier + "'}";
    }
}
