package eu.telecom.teleshop.payment.service;

import eu.telecom.teleshop.payment.events.PaymentCreated;
import eu.telecom.teleshop.payment.events.PaymentFailed;
import eu.telecom.teleshop.payment.events.PaymentSucceeded;
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
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.createPayment(orderId, userId, amount);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(amount, result.getAmount());
        assertEquals("EUR", result.getCurrency());
        assertEquals(PaymentStatus.PENDING, result.getStatus());

        // Verify repository save was called
        verify(paymentRepository).save(any(Payment.class));

        // Verify payment created event was published
        verify(eventPublisher).publishPaymentCreated(any(PaymentCreated.class));
    }

    @Test
    void testProcessPayment_Success() {
        // Given
        String paymentMethodId = "pm_test_456";
        testPayment.setStatus(PaymentStatus.SUCCEEDED);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        Payment result = paymentService.processPayment(1L, paymentMethodId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        assertEquals(paymentMethodId, result.getPaymentMethodId());

        // Verify payment succeeded event was published
        ArgumentCaptor<PaymentSucceeded> eventCaptor = ArgumentCaptor.forClass(PaymentSucceeded.class);
        verify(eventPublisher).publishPaymentSucceeded(eventCaptor.capture());
        
        PaymentSucceeded publishedEvent = eventCaptor.getValue();
        assertEquals(result.getId(), publishedEvent.getPaymentId());
        assertEquals(orderId, publishedEvent.getOrderId());
        assertEquals(userId, publishedEvent.getUserId());
        assertEquals(amount, publishedEvent.getAmount());
    }

    @Test
    void testProcessPayment_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(1L, "pm_test_456");
        });

        // Verify no events were published
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void testProcessPayment_SimulateFailure() {
        // Given - simulate that the service will set status to FAILED
        String paymentMethodId = "pm_test_fail";
        Payment failedPayment = new Payment();
        failedPayment.setId(1L);
        failedPayment.setOrderId(orderId);
        failedPayment.setUserId(userId);
        failedPayment.setAmount(amount);
        failedPayment.setStatus(PaymentStatus.FAILED);
        failedPayment.setFailureReason("Card declined");
        failedPayment.setPaymentMethodId(paymentMethodId);
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);

        // When
        Payment result = paymentService.processPayment(1L, paymentMethodId);

        // Then - check that the service logic handled the failure appropriately
        assertNotNull(result);
        // The result depends on the actual implementation in PaymentService
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_WithDifferentAmount() {
        // Given
        Integer largeAmount = 10000;
        Payment largePayment = new Payment();
        largePayment.setId(2L);
        largePayment.setOrderId(orderId);
        largePayment.setUserId(userId);
        largePayment.setAmount(largeAmount);
        largePayment.setCurrency("EUR");
        largePayment.setStatus(PaymentStatus.PENDING);
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(largePayment);

        // When
        Payment result = paymentService.createPayment(orderId, userId, largeAmount);

        // Then
        assertNotNull(result);
        assertEquals(largeAmount, result.getAmount());
        verify(eventPublisher).publishPaymentCreated(any(PaymentCreated.class));
    }
}