package eu.telecom.teleshop.payment.events;

public class PaymentCreated {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Integer amount;
    private String status;

    public PaymentCreated() {}
    
    public PaymentCreated(Long paymentId, Long orderId, Long userId, Integer amount, String status) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
