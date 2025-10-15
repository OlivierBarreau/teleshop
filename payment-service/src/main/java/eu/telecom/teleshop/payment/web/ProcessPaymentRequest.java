package eu.telecom.teleshop.payment.web;

public class ProcessPaymentRequest {
    private String paymentMethodId;

    public ProcessPaymentRequest() {}

    public ProcessPaymentRequest(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
}
