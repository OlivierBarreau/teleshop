package eu.telecom.teleshop.payment.service;

import eu.telecom.teleshop.payment.exception.MockPaymentsException;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service for handling payment retry logic with exponential backoff
 */
@Service
public class PaymentRetryService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentRetryService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final RetryTemplate paymentRetryTemplate;
    
    public PaymentRetryService(PaymentService paymentService, 
                              PaymentRepository paymentRepository,
                              RetryTemplate paymentRetryTemplate) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.paymentRetryTemplate = paymentRetryTemplate;
    }
    
    /**
     * Process payment with automatic retry logic
     * 
     * Retry behavior:
     * - Attempt 1: Immediate
     * - Attempt 2: After 1 second
     * - Attempt 3: After 2 seconds (exponential backoff)
     * - Attempt 4: After 4 seconds
     * 
     * If all attempts fail, marks payment as FAILED
     */
    public Payment processPaymentWithRetry(Long paymentId, String paymentMethodId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        log.info("Starting payment processing with retry for payment ID: {}", paymentId);
        
        try {
            return paymentRetryTemplate.execute(context -> {
                int attemptNumber = context.getRetryCount() + 1;
                log.info("Payment retry attempt {} of {} for payment ID: {}", 
                        attemptNumber, MAX_RETRY_ATTEMPTS, paymentId);
                
                try {
                    // Attempt to process payment
                    Payment result = paymentService.processPayment(paymentId, paymentMethodId);
                    
                    if (result.getStatus() == PaymentStatus.SUCCEEDED) {
                        log.info("Payment succeeded on attempt {} for payment ID: {}", 
                                attemptNumber, paymentId);
                        return result;
                    } else if (result.getStatus() == PaymentStatus.FAILED) {
                        // Payment explicitly failed (e.g., card declined)
                        // Don't retry explicit failures
                        log.warn("Payment explicitly failed on attempt {} for payment ID: {} - Reason: {}", 
                                attemptNumber, paymentId, result.getFailureReason());
                        return result;
                    }
                    
                    return result;
                    
                } catch (MockPaymentsException e) {
                    log.error("Payment processing failed on attempt {} for payment ID: {} - Error: {}", 
                            attemptNumber, paymentId, e.getMessage());
                    
                    // If this is the last attempt, handle final failure
                    if (attemptNumber >= MAX_RETRY_ATTEMPTS) {
                        return handleFinalFailure(payment, e);
                    }
                    
                    // Throw to trigger retry
                    throw e;
                }
            });
            
        } catch (Exception e) {
            // All retry attempts exhausted
            log.error("All {} retry attempts exhausted for payment ID: {}", 
                    MAX_RETRY_ATTEMPTS, paymentId);
            return handleFinalFailure(payment, e);
        }
    }
    
    /**
     * Handle final failure after all retry attempts exhausted
     */
    private Payment handleFinalFailure(Payment payment, Exception e) {
        log.error("Payment processing failed after {} attempts for payment ID: {}", 
                MAX_RETRY_ATTEMPTS, payment.getId());
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Payment failed after " + MAX_RETRY_ATTEMPTS + 
                " retry attempts: " + e.getMessage());
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Retry failed payment manually
     * Useful for admin operations or scheduled retry jobs
     */
    public Payment retryFailedPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed payments. Current status: " + 
                    payment.getStatus());
        }
        
        if (payment.getPaymentMethodId() == null) {
            throw new IllegalStateException("Cannot retry payment without payment method");
        }
        
        log.info("Manual retry initiated for failed payment ID: {}", paymentId);
        return processPaymentWithRetry(paymentId, payment.getPaymentMethodId());
    }
}
