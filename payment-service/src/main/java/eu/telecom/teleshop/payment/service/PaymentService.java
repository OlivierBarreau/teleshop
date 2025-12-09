package eu.telecom.teleshop.payment.service;

import eu.telecom.teleshop.payment.client.CreatePaymentIntentRequest;
import eu.telecom.teleshop.payment.client.MockPaymentsClient;
import eu.telecom.teleshop.payment.client.PaymentIntent;
import eu.telecom.teleshop.payment.client.PaymentIntentStatus;
import eu.telecom.teleshop.payment.events.OrderPaid;
import eu.telecom.teleshop.payment.events.PaymentCreated;
import eu.telecom.teleshop.payment.events.PaymentFailed;
import eu.telecom.teleshop.payment.events.PaymentSucceeded;
import eu.telecom.teleshop.payment.exception.MockPaymentsException;
import eu.telecom.teleshop.payment.exception.PaymentNotFoundException;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final MockPaymentsClient mockPaymentsClient;
    private final PaymentEventPublisher eventPublisher;
    private final String webhookUrl;
    private final String returnUrl;
    
    public PaymentService(PaymentRepository paymentRepository, 
                         MockPaymentsClient mockPaymentsClient, 
                         PaymentEventPublisher eventPublisher,
                         @Value("${mockpayments.webhook-url:}") String webhookUrl,
                         @Value("${mockpayments.return-url:}") String returnUrl) {
        this.paymentRepository = paymentRepository;
        this.mockPaymentsClient = mockPaymentsClient;
        this.eventPublisher = eventPublisher;
        this.webhookUrl = webhookUrl;
        this.returnUrl = returnUrl;
    }
    
    /**
     * Create a payment for an order
     */
    public Payment createPayment(Long orderId, Long userId, Integer amount) {
        try {
            // Create payment intent request
            CreatePaymentIntentRequest request = new CreatePaymentIntentRequest(amount, "EUR", userId.toString());
            request.setCaptureMethod("manual");
            request.setReturnUrl(returnUrl);
            
            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("orderId", orderId.toString());
            metadata.put("userId", userId.toString());
            request.setMetadata(metadata);
            
            // Create payment intent via MockPayments API
            PaymentIntent intent = mockPaymentsClient.createPaymentIntent(request);
            
            // Create payment entity
            Payment payment = new Payment(
                intent.getId(),
                orderId,
                userId,
                amount,
                "EUR",
                mapPaymentIntentStatus(intent.getStatus())
            );
            
            // Apply payment intent details
            applyPaymentIntentDetails(payment, intent);
            
            // Save payment
            payment = paymentRepository.save(payment);
            
            // Configure webhook if provided
            if (webhookUrl != null && !webhookUrl.isBlank()) {
                try {
                    mockPaymentsClient.configureWebhook(webhookUrl);
                } catch (Exception e) {
                    log.warn("Failed to configure webhook: {}", e.getMessage());
                }
            }
            
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
            
        } catch (MockPaymentsException e) {
            log.error("Failed to create payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment", e);
        }
    }
    
    /**
     * Attach payment method and confirm payment
     */
    public Payment processPayment(Long paymentId, String paymentMethodId) {
        // Find payment by ID or throw PaymentNotFoundException
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        try {
            // Attach payment method
            PaymentIntent intent = mockPaymentsClient.attachPaymentMethod(payment.getPaymentIntentId(), paymentMethodId);
            
            // Apply updates from payment intent
            applyPaymentIntentDetails(payment, intent);
            payment.setPaymentMethodId(paymentMethodId);
            
            // Confirm payment intent
            intent = mockPaymentsClient.confirmPaymentIntent(payment.getPaymentIntentId());
            applyPaymentIntentDetails(payment, intent);
            
            // Save payment
            payment = paymentRepository.save(payment);
            
            // If payment succeeded, publish events
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                publishPaymentSucceededEvent(payment);
            }
            
            return payment;
            
        } catch (MockPaymentsException e) {
            log.error("Failed to process payment: {}", e.getMessage(), e);
            handlePaymentFailure(payment, e.getMessage());
            payment = paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    public Payment capturePayment(Long paymentId, Integer amountToCapture) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        try {
            PaymentIntent intent = mockPaymentsClient.capturePaymentIntent(payment.getPaymentIntentId(), amountToCapture);
            applyPaymentIntentDetails(payment, intent);
            payment = paymentRepository.save(payment);
            
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                publishPaymentSucceededEvent(payment);
            }
            
            return payment;
        } catch (MockPaymentsException e) {
            handlePaymentFailure(payment, e.getMessage());
            payment = paymentRepository.save(payment);
            throw e;
        }
    }

    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        try {
            PaymentIntent intent = mockPaymentsClient.cancelPaymentIntent(payment.getPaymentIntentId());
            applyPaymentIntentDetails(payment, intent);
            payment = paymentRepository.save(payment);
            return payment;
        } catch (MockPaymentsException e) {
            handlePaymentFailure(payment, e.getMessage());
            payment = paymentRepository.save(payment);
            throw e;
        }
    }

    public Payment refreshPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        try {
            PaymentIntent intent = mockPaymentsClient.getPaymentIntent(payment.getPaymentIntentId());
            if (intent != null) {
                applyPaymentIntentDetails(payment, intent);
                payment = paymentRepository.save(payment);
                
                if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                    publishPaymentSucceededEvent(payment);
                } else if (payment.getStatus() == PaymentStatus.FAILED) {
                    publishPaymentFailedEvent(payment);
                }
            }
            return payment;
        } catch (MockPaymentsException e) {
            log.error("Failed to refresh payment status: {}", e.getMessage());
            throw e;
        }
    }

    public void handleWebhookEvent(String eventType, String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            log.warn("Received webhook with empty payment intent ID");
            return;
        }

        paymentRepository.findByPaymentIntentId(paymentIntentId).ifPresentOrElse(payment -> {
            try {
                log.info("Processing webhook {} for payment {}", eventType, payment.getId());
                
                // Refresh payment status from API
                PaymentIntent intent = mockPaymentsClient.getPaymentIntent(paymentIntentId);
                if (intent != null) {
                    PaymentStatus oldStatus = payment.getStatus();
                    applyPaymentIntentDetails(payment, intent);
                    paymentRepository.save(payment);
                    
                    // Publish events based on status change
                    if (payment.getStatus() == PaymentStatus.SUCCEEDED && oldStatus != PaymentStatus.SUCCEEDED) {
                        publishPaymentSucceededEvent(payment);
                    } else if (payment.getStatus() == PaymentStatus.FAILED && oldStatus != PaymentStatus.FAILED) {
                        publishPaymentFailedEvent(payment);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to handle webhook for payment intent {}: {}", paymentIntentId, e.getMessage());
            }
        }, () -> log.warn("Received webhook {} for unknown payment intent {}", eventType, paymentIntentId));
    }

    
    /**
     * Helper method to map PaymentIntentStatus to PaymentStatus
     */
    private PaymentStatus mapPaymentIntentStatus(PaymentIntentStatus status) {
        return switch (status) {
            case REQUIRES_PAYMENT_METHOD -> PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case REQUIRES_CONFIRMATION -> PaymentStatus.REQUIRES_CONFIRMATION;
            case REQUIRES_ACTION -> PaymentStatus.REQUIRES_ACTION;
            case PROCESSING -> PaymentStatus.PROCESSING;
            case SUCCEEDED -> PaymentStatus.SUCCEEDED;
            case CANCELED -> PaymentStatus.CANCELED;
            case FAILED -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private void applyPaymentIntentDetails(Payment payment, PaymentIntent intent) {
        if (intent == null) return;
        
        payment.setStatus(mapPaymentIntentStatus(intent.getStatus()));
        payment.setClientSecret(intent.getClientSecret());
        payment.setCaptureMethod(intent.getCaptureMethod());
        payment.setReturnUrl(intent.getReturnUrl());
        
        if (intent.getNextAction() != null) {
            payment.setNextActionType(intent.getNextAction().getType());
            payment.setNextActionUrl(intent.getNextAction().getUrl());
        } else {
            payment.setNextActionType(null);
            payment.setNextActionUrl(null);
        }
        
        if (intent.getLastPaymentError() != null) {
            payment.setLastPaymentError(intent.getLastPaymentError().getMessage());
            payment.setFailureCode(intent.getLastPaymentError().getCode());
            payment.setFailureParam(intent.getLastPaymentError().getParam());
            payment.setFailureReason(intent.getLastPaymentError().getMessage());
        } else {
            payment.setLastPaymentError(null);
            payment.setFailureCode(null);
            payment.setFailureParam(null);
        }
    }

    private void handlePaymentFailure(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        publishPaymentFailedEvent(payment);
    }

    private void publishPaymentSucceededEvent(Payment payment) {
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
    }

    private void publishPaymentFailedEvent(Payment payment) {
        PaymentFailed event = new PaymentFailed(
            payment.getId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getAmount(),
            payment.getFailureReason()
        );
        eventPublisher.publishPaymentFailed(event);
    }
}
