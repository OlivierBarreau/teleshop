package eu.telecom.teleshop.payment.exception;

/**
 * Exception thrown when a payment is not found
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(Long paymentId) {
        super("Payment not found: " + paymentId);
    }
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
    
    public PaymentNotFoundException(String field, Object value) {
        super("Payment not found with " + field + ": " + value);
    }
}