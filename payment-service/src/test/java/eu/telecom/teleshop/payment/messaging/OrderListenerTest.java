package eu.telecom.teleshop.payment.messaging;

import eu.telecom.teleshop.payment.events.OrderCreated;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentListener paymentListener;

    private OrderCreated orderCreatedEvent;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        orderCreatedEvent = new OrderCreated();
        orderCreatedEvent.setOrderId(1L);
        orderCreatedEvent.setUserId(100L);
        orderCreatedEvent.setProductId(5L);
        orderCreatedEvent.setQuantity(3);

        mockPayment = new Payment();
        mockPayment.setId(1L);
        mockPayment.setOrderId(1L);
        mockPayment.setUserId(100L);
        mockPayment.setAmount(3000); // 3 items * €10.00 each
        mockPayment.setCurrency("EUR");
        mockPayment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void testHandleOrderCreated_Success() {
        // Given
        when(paymentService.createPayment(eq(1L), eq(100L), eq(3000)))
                .thenReturn(mockPayment);

        // When
        paymentListener.handleOrderCreated(orderCreatedEvent);

        // Then
        ArgumentCaptor<Long> orderIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(paymentService).createPayment(
                orderIdCaptor.capture(),
                userIdCaptor.capture(),
                amountCaptor.capture()
        );

        assertEquals(1L, orderIdCaptor.getValue());
        assertEquals(100L, userIdCaptor.getValue());
        assertEquals(3000, amountCaptor.getValue()); // 3 * 1000 cents = €30.00
    }

    @Test
    void testHandleOrderCreated_AmountCalculation() {
        // Given - different quantities to test calculation
        OrderCreated event1 = new OrderCreated();
        event1.setOrderId(2L);
        event1.setUserId(101L);
        event1.setProductId(5L);
        event1.setQuantity(1); // Should result in 1000 cents (€10.00)

        OrderCreated event2 = new OrderCreated();
        event2.setOrderId(3L);
        event2.setUserId(102L);
        event2.setProductId(6L);
        event2.setQuantity(5); // Should result in 5000 cents (€50.00)

        when(paymentService.createPayment(any(), any(), any()))
                .thenReturn(mockPayment);

        // When
        paymentListener.handleOrderCreated(event1);
        paymentListener.handleOrderCreated(event2);

        // Then
        verify(paymentService).createPayment(eq(2L), eq(101L), eq(1000));
        verify(paymentService).createPayment(eq(3L), eq(102L), eq(5000));
    }

    @Test
    void testHandleOrderCreated_PaymentServiceException() {
        // Given
        when(paymentService.createPayment(any(), any(), any()))
                .thenThrow(new RuntimeException("Payment service unavailable"));

        // When & Then - Should not throw exception (error handling in listener)
        assertDoesNotThrow(() -> {
            paymentListener.handleOrderCreated(orderCreatedEvent);
        });

        // Verify service was called despite exception
        verify(paymentService).createPayment(eq(1L), eq(100L), eq(3000));
    }

    @Test
    void testHandleOrderCreated_NullEvent() {
        // When & Then
        assertDoesNotThrow(() -> {
            paymentListener.handleOrderCreated(null);
        });

        // Verify service was not called
        verifyNoInteractions(paymentService);
    }

    @Test
    void testHandleOrderCreated_ZeroQuantity() {
        // Given
        orderCreatedEvent.setQuantity(0);

        when(paymentService.createPayment(any(), any(), any()))
                .thenReturn(mockPayment);

        // When
        paymentListener.handleOrderCreated(orderCreatedEvent);

        // Then
        verify(paymentService).createPayment(eq(1L), eq(100L), eq(0));
    }

    @Test
    void testHandleOrderCreated_NegativeQuantity() {
        // Given
        orderCreatedEvent.setQuantity(-1);

        when(paymentService.createPayment(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Amount must be positive"));

        // When & Then - Should handle exception gracefully
        assertDoesNotThrow(() -> {
            paymentListener.handleOrderCreated(orderCreatedEvent);
        });

        verify(paymentService).createPayment(eq(1L), eq(100L), eq(-1000));
    }

    @Test
    void testHandleOrderCreated_LargeQuantity() {
        // Given
        orderCreatedEvent.setQuantity(1000); // Large order

        when(paymentService.createPayment(any(), any(), any()))
                .thenReturn(mockPayment);

        // When
        paymentListener.handleOrderCreated(orderCreatedEvent);

        // Then
        verify(paymentService).createPayment(eq(1L), eq(100L), eq(1000000)); // €10,000.00
    }
}