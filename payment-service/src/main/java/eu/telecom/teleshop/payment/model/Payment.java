package eu.telecom.teleshop.payment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String paymentIntentId;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer amount; // Amount in cents
    
    @Column(nullable = false)
    private String currency = "EUR";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String paymentMethodId;
    
    private String failureReason;
    
    // Additional fields for MockPayments integration
    private String clientSecret;
    
    private String captureMethod;
    
    private String returnUrl;
    
    private String nextActionType;
    
    private String nextActionUrl;
    
    private String lastPaymentError;
    
    private String failureCode;
    
    private String failureParam;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // Constructors
    public Payment() {}
    
    public Payment(String paymentIntentId, Long orderId, Long userId, Integer amount, String currency, PaymentStatus status) {
        this.paymentIntentId = paymentIntentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { 
        this.paymentMethodId = paymentMethodId;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { 
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Additional getters and setters for MockPayments integration
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { 
        this.clientSecret = clientSecret;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCaptureMethod() { return captureMethod; }
    public void setCaptureMethod(String captureMethod) { 
        this.captureMethod = captureMethod;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { 
        this.returnUrl = returnUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getNextActionType() { return nextActionType; }
    public void setNextActionType(String nextActionType) { 
        this.nextActionType = nextActionType;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getNextActionUrl() { return nextActionUrl; }
    public void setNextActionUrl(String nextActionUrl) { 
        this.nextActionUrl = nextActionUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLastPaymentError() { return lastPaymentError; }
    public void setLastPaymentError(String lastPaymentError) { 
        this.lastPaymentError = lastPaymentError;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { 
        this.failureCode = failureCode;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFailureParam() { return failureParam; }
    public void setFailureParam(String failureParam) { 
        this.failureParam = failureParam;
        this.updatedAt = LocalDateTime.now();
    }
}
