package eu.telecom.teleshop.payment.model;

public enum PaymentStatus {
    PENDING,
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REFUNDED
}
