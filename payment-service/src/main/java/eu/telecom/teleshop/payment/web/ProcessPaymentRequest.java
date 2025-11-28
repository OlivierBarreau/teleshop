package eu.telecom.teleshop.payment.web;

import jakarta.validation.constraints.NotBlank;

public class ProcessPaymentRequest {

    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;

    public ProcessPaymentRequest() {}

    public ProcessPaymentRequest(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Override
    public String toString() {
        return "ProcessPaymentRequest{" +
                "paymentMethodId='" + paymentMethodId + '\'' +
                '}';
    }
}
