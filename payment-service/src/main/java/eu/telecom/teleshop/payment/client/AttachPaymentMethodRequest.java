package eu.telecom.teleshop.payment.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttachPaymentMethodRequest {

    @JsonProperty("payment_method")
    private String paymentMethod;
    
    public AttachPaymentMethodRequest() {}
    
    public AttachPaymentMethodRequest(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}