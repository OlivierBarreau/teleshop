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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;
    
    @Mock
    private MockPaymentsClient mockPaymentsClient;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;
    private Long orderId;
    private Long userId;
    private Integer amount;

    @BeforeEach
    void setUp() {
        orderId = 1L;
        userId = 100L;
        amount = 2000; // €20.00 in cents

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(orderId);
        testPayment.setUserId(userId);
        testPayment.setAmount(amount);
        testPayment.setCurrency("EUR");
        testPayment.setStatus(PaymentStatus.PENDING);
        testPayment.setPaymentIntentId("pi_test_123");
    }

    @Test
    void testCreatePayment_Success() {
        // Given
        PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_test_123");
        mockIntent.setAmount(amount);
        mockIntent.setCurrency("EUR");
        mockIntent.setStatus(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);
        mockIntent.setClientSecret("pi_test_123_secret");
        
        when(mockPaymentsClient.createPaymentIntent(any(CreatePaymentIntentRequest.class))).thenReturn(mockIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.createPayment(orderId, userId, amount);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(amount, result.getAmount());
        assertEquals("EUR", result.getCurrency());

        // Verify MockPayments client was called
        verify(mockPaymentsClient).createPaymentIntent(any(CreatePaymentIntentRequest.class));
        
        // Verify repository save was called
        verify(paymentRepository).save(any(Payment.class));

        // Verify payment created event was published
        verify(eventPublisher).publishPaymentCreated(any(PaymentCreated.class));
    }

    @Test
    void testProcessPayment_Success() {
        // Given
        String paymentMethodId = "pm_test_456";
        testPayment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        
        // Mock attach payment method response
        PaymentIntent attachedIntent = new PaymentIntent();
        attachedIntent.setId("pi_test_123");
        attachedIntent.setStatus(PaymentIntentStatus.REQUIRES_CONFIRMATION);
        attachedIntent.setPaymentMethod(paymentMethodId);
        
        // Mock confirm payment intent response
        PaymentIntent confirmedIntent = new PaymentIntent();
        confirmedIntent.setId("pi_test_123");
        confirmedIntent.setStatus(PaymentIntentStatus.SUCCEEDED);
        confirmedIntent.setPaymentMethod(paymentMethodId);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(mockPaymentsClient.attachPaymentMethod("pi_test_123", paymentMethodId)).thenReturn(attachedIntent);
        when(mockPaymentsClient.confirmPaymentIntent("pi_test_123")).thenReturn(confirmedIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.processPayment(1L, paymentMethodId);

        // Then
        assertNotNull(result);
        assertEquals(paymentMethodId, result.getPaymentMethodId());

        // Verify MockPayments client calls
        verify(mockPaymentsClient).attachPaymentMethod("pi_test_123", paymentMethodId);
        verify(mockPaymentsClient).confirmPaymentIntent("pi_test_123");

        // Verify payment succeeded event was published
        verify(eventPublisher).publishPaymentSucceeded(any(PaymentSucceeded.class));
        verify(eventPublisher).publishOrderPaid(any(OrderPaid.class));
    }

    @Test
    void testProcessPayment_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.processPayment(1L, "pm_test_456");
        });

        // Verify no events were published
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testProcessPayment_SimulateFailure() {
        // Given - Mock PaymentService to throw MockPaymentsException
        String paymentMethodId = "pm_test_fail";
        testPayment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(mockPaymentsClient.attachPaymentMethod("pi_test_123", paymentMethodId))
            .thenThrow(new MockPaymentsException("Payment declined"));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(1L, paymentMethodId);
        });

        // Verify MockPayments client was called
        verify(mockPaymentsClient).attachPaymentMethod("pi_test_123", paymentMethodId);
        
        // Verify repository save was called (for failure handling)
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_WithDifferentAmount() {
        // Given
        Integer largeAmount = 10000;
        
        PaymentIntent mockIntent = new PaymentIntent();
        mockIntent.setId("pi_test_large_456");
        mockIntent.setAmount(largeAmount);
        mockIntent.setCurrency("EUR");
        mockIntent.setStatus(PaymentIntentStatus.REQUIRES_PAYMENT_METHOD);
        
        Payment largePayment = new Payment();
        largePayment.setId(2L);
        largePayment.setOrderId(orderId);
        largePayment.setUserId(userId);
        largePayment.setAmount(largeAmount);
        largePayment.setCurrency("EUR");
        largePayment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        
        when(mockPaymentsClient.createPaymentIntent(any(CreatePaymentIntentRequest.class))).thenReturn(mockIntent);
        when(paymentRepository.save(any(Payment.class))).thenReturn(largePayment);

        // When
        Payment result = paymentService.createPayment(orderId, userId, largeAmount);

        // Then
        assertNotNull(result);
        assertEquals(largeAmount, result.getAmount());
        verify(mockPaymentsClient).createPaymentIntent(any(CreatePaymentIntentRequest.class));
        verify(eventPublisher).publishPaymentCreated(any(PaymentCreated.class));
    }

    /**
     * Exercise 11.1: Test case for payment creation failure when MockPayments API is unavailable
     * 
     * What should happen when the MockPayments API is unavailable?
     * Answer: The system should:
     * 1. Throw a RuntimeException wrapping the MockPaymentsException
     * 2. NOT save any payment record to the database (transaction rollback)
     * 3. NOT publish any payment events (to prevent misleading notifications)
     * 4. Allow the caller (e.g., OrderListener) to handle the error appropriately
     * 5. Enable retry mechanisms at the message queue level (RabbitMQ)
     * 
     * This ensures data consistency and allows for graceful degradation when external
     * dependencies are unavailable. The order can remain in pending state until the
     * payment service becomes available again.
     */
    @Test
    void testCreatePayment_ApiUnavailable() {
        // Given - MockPayments API is unavailable (throws exception)
        when(mockPaymentsClient.createPaymentIntent(any(CreatePaymentIntentRequest.class)))
            .thenThrow(new MockPaymentsException("Payment service unavailable"));

        // When & Then - Should throw RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(orderId, userId, amount);
        });

        // Verify exception message contains context
        assertTrue(exception.getMessage().contains("Failed to create payment"));
        assertTrue(exception.getCause() instanceof MockPaymentsException);
        assertEquals("Payment service unavailable", exception.getCause().getMessage());

        // Verify MockPayments client was called (attempt was made)
        verify(mockPaymentsClient).createPaymentIntent(any(CreatePaymentIntentRequest.class));
        
        // Verify NO payment was saved to database (transaction rollback)
        verify(paymentRepository, never()).save(any(Payment.class));
        
        // Verify NO events were published (prevent misleading notifications)
        verify(eventPublisher, never()).publishPaymentCreated(any(PaymentCreated.class));
    }

    /**
     * Additional test: Network timeout during payment creation
     */
    @Test
    void testCreatePayment_NetworkTimeout() {
        // Given - Simulate network timeout
        when(mockPaymentsClient.createPaymentIntent(any(CreatePaymentIntentRequest.class)))
            .thenThrow(new MockPaymentsException("Connection timeout after 30s"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(orderId, userId, amount);
        });

        assertTrue(exception.getCause().getMessage().contains("Connection timeout"));
        
        // Verify no side effects occurred
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishPaymentCreated(any(PaymentCreated.class));
    }

    /**
     * Additional test: Invalid API credentials
     */
    @Test
    void testCreatePayment_InvalidCredentials() {
        // Given - Invalid API key
        when(mockPaymentsClient.createPaymentIntent(any(CreatePaymentIntentRequest.class)))
            .thenThrow(new MockPaymentsException("Invalid API key"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(orderId, userId, amount);
        });

        assertTrue(exception.getCause().getMessage().contains("Invalid API key"));
        
        // Verify no payment record was created
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}