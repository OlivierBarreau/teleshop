package eu.telecom.teleshop.payment.client;

import java.util.Map;

public class CreatePaymentIntentRequest {
    private Integer amount;
    private String currency;
    private String customer;
    private String captureMethod;
    private String returnUrl;
    private Map<String, String> metadata;
    
    public CreatePaymentIntentRequest() {}
    
    public CreatePaymentIntentRequest(Integer amount, String currency, String customer) {
        this.amount = amount;
        this.currency = currency;
        this.customer = customer;
    }
    
    // Getters and Setters
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getCustomer() {
        return customer;
    }
    
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCaptureMethod() {
        return captureMethod;
    }

    public void setCaptureMethod(String captureMethod) {
        this.captureMethod = captureMethod;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}