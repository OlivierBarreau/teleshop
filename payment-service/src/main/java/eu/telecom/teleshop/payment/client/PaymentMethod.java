package eu.telecom.teleshop.payment.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentMethod {
    private String id;
    private String type;
    private Card card;
    @JsonProperty("created_at")
    private String createdAt;

    public PaymentMethod() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static class Card {
        private String brand;
        private String last4;
        @JsonProperty("exp_month")
        private Integer expMonth;
        @JsonProperty("exp_year")
        private Integer expYear;

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getLast4() {
            return last4;
        }

        public void setLast4(String last4) {
            this.last4 = last4;
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
    }
}