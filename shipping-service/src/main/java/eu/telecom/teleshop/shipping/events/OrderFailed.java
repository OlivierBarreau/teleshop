package eu.telecom.teleshop.shipping.events;

public class OrderFailed {
    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String reason;

    public OrderFailed() {}

    public OrderFailed(Long orderId, Long userId, Long productId, Integer quantity, String reason) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "OrderFailed{orderId=" + orderId + ", userId=" + userId + 
               ", productId=" + productId + ", quantity=" + quantity + 
               ", reason='" + reason + "'}";
    }
}
