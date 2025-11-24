package eu.telecom.teleshop.notification.events;

public class PaymentFailed {
        private Long paymentId;
    private Long orderId;
    private Long userId;
    private Integer amount;
    private String reason;

    public PaymentFailed() {}

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

}
