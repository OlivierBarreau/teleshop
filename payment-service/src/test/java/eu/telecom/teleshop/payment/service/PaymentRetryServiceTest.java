package eu.telecom.teleshop.payment.service;

import eu.telecom.teleshop.payment.client.MockPaymentsClient;
import eu.telecom.teleshop.payment.client.PaymentIntent;
import eu.telecom.teleshop.payment.client.PaymentIntentStatus;
import eu.telecom.teleshop.payment.exception.MockPaymentsException;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for PaymentRetryService - Testing automatic retry logic with exponential backoff
 */
@ExtendWith(MockitoExtension.class)
class PaymentRetryServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RetryTemplate paymentRetryTemplate;

    @InjectMocks
    private PaymentRetryService paymentRetryService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(1L);
        testPayment.setUserId(100L);
        testPayment.setAmount(2000);
        testPayment.setCurrency("EUR");
        testPayment.setStatus(PaymentStatus.REQUIRES_PAYMENT_METHOD);
        testPayment.setPaymentIntentId("pi_test_123");
    }

    @Test
    void testProcessPaymentWithRetry_SuccessOnFirstAttempt() throws Exception {
        // Given
        String paymentMethodId = "pm_test_success";
        Payment succeededPayment = new Payment();
        succeededPayment.setId(1L);
        succeededPayment.setStatus(PaymentStatus.SUCCEEDED);
        succeededPayment.setPaymentMethodId(paymentMethodId);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        
        // Configure RetryTemplate to execute the callback and return the result
        when(paymentRetryTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.retry.RetryCallback<Payment, Exception> callback = invocation.getArgument(0);
            org.springframework.retry.RetryContext context = mock(org.springframework.retry.RetryContext.class);
            when(context.getRetryCount()).thenReturn(0); // First attempt
            return callback.doWithRetry(context);
        });
        
        when(paymentService.processPayment(1L, paymentMethodId)).thenReturn(succeededPayment);

        // When
        Payment result = paymentRetryService.processPaymentWithRetry(1L, paymentMethodId);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        verify(paymentService, times(1)).processPayment(1L, paymentMethodId);
    }

    @Test
    void testProcessPaymentWithRetry_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentRetryService.processPaymentWithRetry(999L, "pm_test_123");
        });

        verify(paymentService, never()).processPayment(any(), any());
    }

    @Test
    void testRetryFailedPayment_Success() throws Exception {
        // Given
        testPayment.setStatus(PaymentStatus.FAILED);
        testPayment.setPaymentMethodId("pm_test_retry");
        
        Payment succeededPayment = new Payment();
        succeededPayment.setId(1L);
        succeededPayment.setStatus(PaymentStatus.SUCCEEDED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        
        when(paymentRetryTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.retry.RetryCallback<Payment, Exception> callback = invocation.getArgument(0);
            org.springframework.retry.RetryContext context = mock(org.springframework.retry.RetryContext.class);
            when(context.getRetryCount()).thenReturn(0);
            return callback.doWithRetry(context);
        });
        
        when(paymentService.processPayment(1L, "pm_test_retry")).thenReturn(succeededPayment);

        // When
        Payment result = paymentRetryService.retryFailedPayment(1L);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
    }

    @Test
    void testRetryFailedPayment_NotInFailedStatus() {
        // Given
        testPayment.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentRetryService.retryFailedPayment(1L);
        });

        assertTrue(exception.getMessage().contains("Can only retry failed payments"));
        verify(paymentService, never()).processPayment(any(), any());
    }

    @Test
    void testRetryFailedPayment_NoPaymentMethod() {
        // Given
        testPayment.setStatus(PaymentStatus.FAILED);
        testPayment.setPaymentMethodId(null);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentRetryService.retryFailedPayment(1L);
        });

        assertTrue(exception.getMessage().contains("Cannot retry payment without payment method"));
        verify(paymentService, never()).processPayment(any(), any());
    }

    @Test
    void testRetryFailedPayment_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentRetryService.retryFailedPayment(999L);
        });
    }
}
