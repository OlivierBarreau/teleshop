package eu.telecom.teleshop.payment.exception;

/**
 * Exception thrown when there's an error communicating with the MockPayments API
 */
public class MockPaymentsException extends RuntimeException {
    
    public MockPaymentsException(String message) {
        super(message);
    }
    
    public MockPaymentsException(String message, Throwable cause) {
        super(message, cause);
    }
}