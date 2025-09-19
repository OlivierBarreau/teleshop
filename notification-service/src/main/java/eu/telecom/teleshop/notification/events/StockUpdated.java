package eu.telecom.teleshop.notification.events;

public class StockUpdated {
    private Long orderId;
    private Long productId;
    private Integer quantity;

    public StockUpdated() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
