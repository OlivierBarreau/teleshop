package eu.telecom.teleshop.payment.client;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapturePaymentIntentRequest {

    private Integer amountToCapture;

    public CapturePaymentIntentRequest() {
    }

    public CapturePaymentIntentRequest(Integer amountToCapture) {
        this.amountToCapture = amountToCapture;
    }

    public Integer getAmountToCapture() {
        return amountToCapture;
    }

    public void setAmountToCapture(Integer amountToCapture) {
        this.amountToCapture = amountToCapture;
    }
}