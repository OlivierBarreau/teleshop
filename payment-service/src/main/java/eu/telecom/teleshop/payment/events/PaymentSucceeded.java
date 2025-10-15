package eu.telecom.teleshop.payment.events;

public class PaymentSucceeded {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Integer amount;

    public PaymentSucceeded() {}
    
    public PaymentSucceeded(Long paymentId, Long orderId, Long userId, Integer amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
