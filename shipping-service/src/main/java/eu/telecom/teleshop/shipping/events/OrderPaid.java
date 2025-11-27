package eu.telecom.teleshop.shipping.events;

public class OrderPaid {
    private Long orderId;
    private Long userId;
    private Long paymentId;
    private Integer amount;

    public OrderPaid() {}
    
    public OrderPaid(Long orderId, Long userId, Long paymentId, Integer amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    @Override
    public String toString() {
        return "OrderPaid{orderId=" + orderId + ", userId=" + userId + 
               ", paymentId=" + paymentId + ", amount=" + amount + "}";
    }
}
