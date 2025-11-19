package eu.telecom.teleshop.payment.service;

import eu.telecom.teleshop.payment.events.OrderPaid;
import eu.telecom.teleshop.payment.events.PaymentCreated;
import eu.telecom.teleshop.payment.events.PaymentFailed;
import eu.telecom.teleshop.payment.events.PaymentSucceeded;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    
    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Create a payment for an order
     */
    public Payment createPayment(Long orderId, Long userId, Integer amount) {
        // Simulate creating a payment intent ID
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Create payment entity
        Payment payment = new Payment(
            paymentIntentId,
            orderId,
            userId,
            amount,
            "EUR",
            PaymentStatus.REQUIRES_PAYMENT_METHOD
        );
        
        // Save payment
        payment = paymentRepository.save(payment);
        
        // Publish payment created event
        PaymentCreated event = new PaymentCreated(
            payment.getId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getStatus().toString()
        );
        eventPublisher.publishPaymentCreated(event);
        
        return payment;
    }
    
    /**
     * Process payment - simplified version without external API
     */
    public Payment processPayment(Long paymentId, String paymentMethodId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        try {
            // Set payment method
            payment.setPaymentMethodId(paymentMethodId);
            
            // Simulate payment processing - successful for test card numbers
            if (paymentMethodId != null && paymentMethodId.contains("test")) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment = paymentRepository.save(payment);
                
                // Publish success event
                PaymentSucceeded event = new PaymentSucceeded(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount()
                );
                eventPublisher.publishPaymentSucceeded(event);
                
                // Publish OrderPaid event for downstream services (shipping)
                OrderPaid orderPaidEvent = new OrderPaid(
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getId(),
                    payment.getAmount()
                );
                eventPublisher.publishOrderPaid(orderPaidEvent);
            } else {
                // Simulate failure
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Invalid payment method");
                payment = paymentRepository.save(payment);
                
                // Publish failure event
                PaymentFailed event = new PaymentFailed(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    payment.getFailureReason()
                );
                eventPublisher.publishPaymentFailed(event);
            }
            
            return payment;
            
        } catch (Exception e) {
            // Handle payment failure
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            payment = paymentRepository.save(payment);
            
            PaymentFailed event = new PaymentFailed(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getFailureReason()
            );
            eventPublisher.publishPaymentFailed(event);
            
            throw new RuntimeException("Payment processing failed", e);
        }
    }
}
