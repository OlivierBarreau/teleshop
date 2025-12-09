package eu.telecom.teleshop.payment.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PaymentIntent {
    private String id;
    private Integer amount;
    private String currency;
    private PaymentIntentStatus status;
    private String customer;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("capture_method")
    private String captureMethod;
    @JsonProperty("return_url")
    private String returnUrl;
    private Map<String, String> metadata;
    @JsonProperty("last_payment_error")
    private LastPaymentError lastPaymentError;
    @JsonProperty("next_action")
    private NextAction nextAction;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;

    public PaymentIntent() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public PaymentIntentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentIntentStatus status) {
        this.status = status;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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

    public LastPaymentError getLastPaymentError() {
        return lastPaymentError;
    }

    public void setLastPaymentError(LastPaymentError lastPaymentError) {
        this.lastPaymentError = lastPaymentError;
    }

    public NextAction getNextAction() {
        return nextAction;
    }

    public void setNextAction(NextAction nextAction) {
        this.nextAction = nextAction;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class LastPaymentError {
        private String code;
        private String message;
        private String param;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }

    public static class NextAction {
        private String type;
        private String url;
        @JsonProperty("redirect_to_url")
        private RedirectToUrl redirectToUrl;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            if (url != null) {
                return url;
            }
            if (redirectToUrl != null) {
                return redirectToUrl.getUrl();
            }
            return null;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public RedirectToUrl getRedirectToUrl() {
            return redirectToUrl;
        }

        public void setRedirectToUrl(RedirectToUrl redirectToUrl) {
            this.redirectToUrl = redirectToUrl;
        }

        public static class RedirectToUrl {
            private String url;
            @JsonProperty("return_url")
            private String returnUrl;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getReturnUrl() {
                return returnUrl;
            }

            public void setReturnUrl(String returnUrl) {
                this.returnUrl = returnUrl;
            }
        }
    }
}