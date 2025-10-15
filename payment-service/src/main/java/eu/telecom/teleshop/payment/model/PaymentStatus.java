package eu.telecom.teleshop.payment.model;

public enum PaymentStatus {
    PENDING,
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REFUNDED
}
