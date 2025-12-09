package eu.telecom.teleshop.payment.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePaymentMethodRequest {
    private String type;
    private CardDetails card;

    public CreatePaymentMethodRequest() {}

    public CreatePaymentMethodRequest(String type, CardDetails card) {
        this.type = type;
        this.card = card;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CardDetails getCard() {
        return card;
    }

    public void setCard(CardDetails card) {
        this.card = card;
    }

    public static class CardDetails {
        private String number;
        @JsonProperty("exp_month")
        private Integer expMonth;
        @JsonProperty("exp_year")
        private Integer expYear;
        private String cvc;

        public CardDetails() {}

        public CardDetails(String number, Integer expMonth, Integer expYear, String cvc) {
            this.number = number;
            this.expMonth = expMonth;
            this.expYear = expYear;
            this.cvc = cvc;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public Integer getExpMonth() {
            return expMonth;
        }

        public void setExpMonth(Integer expMonth) {
            this.expMonth = expMonth;
        }

        public Integer getExpYear() {
            return expYear;
        }

        public void setExpYear(Integer expYear) {
            this.expYear = expYear;
        }

        public String getCvc() {
            return cvc;
        }

        public void setCvc(String cvc) {
            this.cvc = cvc;
        }
    }
}