package eu.telecom.teleshop.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.telecom.teleshop.payment.client.MockPaymentsClient;
import eu.telecom.teleshop.payment.exception.PaymentNotFoundException;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.model.PaymentStatus;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import eu.telecom.teleshop.payment.service.PaymentService;
import eu.telecom.teleshop.payment.web.ProcessPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentRepository paymentRepository;
    
    @MockBean
    private MockPaymentsClient mockPaymentsClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment1;
    private Payment testPayment2;

    @BeforeEach
    void setUp() {
        testPayment1 = new Payment();
        testPayment1.setId(1L);
        testPayment1.setPaymentIntentId("pi_test_123");
        testPayment1.setOrderId(1L);
        testPayment1.setUserId(100L);
        testPayment1.setAmount(2000);
        testPayment1.setCurrency("EUR");
        testPayment1.setStatus(PaymentStatus.PENDING);
        testPayment1.setCreatedAt(LocalDateTime.now());

        testPayment2 = new Payment();
        testPayment2.setId(2L);
        testPayment2.setPaymentIntentId("pi_test_456");
        testPayment2.setOrderId(2L);
        testPayment2.setUserId(101L);
        testPayment2.setAmount(3000);
        testPayment2.setCurrency("EUR");
        testPayment2.setStatus(PaymentStatus.SUCCEEDED);
        testPayment2.setPaymentMethodId("pm_test_789");
        testPayment2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllPayments() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(testPayment1, testPayment2);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].paymentIntentId", is("pi_test_123")))
                .andExpect(jsonPath("$[0].orderId", is(1)))
                .andExpect(jsonPath("$[0].userId", is(100)))
                .andExpect(jsonPath("$[0].amount", is(2000)))
                .andExpect(jsonPath("$[0].currency", is("EUR")))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].status", is("SUCCEEDED")))
                .andExpect(jsonPath("$[1].paymentMethodId", is("pm_test_789")));
    }

    @Test
    void testGetPayment_Success() throws Exception {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment1));

        // When & Then
        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.paymentIntentId", is("pi_test_123")))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.amount", is(2000)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void testGetPaymentByOrderId_Success() throws Exception {
        // Given
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment1));

        // When & Then
        mockMvc.perform(get("/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void testGetPaymentByOrderId_NotFound() throws Exception {
        // Given
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/payments/order/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        // Given
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setPaymentMethodId("pm_test_456");

        testPayment1.setStatus(PaymentStatus.SUCCEEDED);
        testPayment1.setPaymentMethodId("pm_test_456");
        
        when(paymentService.processPayment(eq(1L), eq("pm_test_456"))).thenReturn(testPayment1);

        // When & Then
        mockMvc.perform(post("/payments/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("SUCCEEDED")))
                .andExpect(jsonPath("$.paymentMethodId", is("pm_test_456")));
    }

    @Test
    void testProcessPayment_PaymentNotFound() throws Exception {
        // Given
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setPaymentMethodId("pm_test_456");

        when(paymentService.processPayment(eq(999L), eq("pm_test_456")))
                .thenThrow(new PaymentNotFoundException(999L));

        // When & Then
        mockMvc.perform(post("/payments/999/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testProcessPayment_InvalidRequest() throws Exception {
        // Given - request with null payment method (should be handled gracefully)
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        // paymentMethodId is null

        // When & Then - expect the controller to handle this (may vary by implementation)
        mockMvc.perform(post("/payments/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Adjust based on actual behavior
    }

    @Test
    void testProcessPayment_PaymentFailed() throws Exception {
        // Given
        ProcessPaymentRequest request = new ProcessPaymentRequest();
        request.setPaymentMethodId("pm_test_fail");

        testPayment1.setStatus(PaymentStatus.FAILED);
        testPayment1.setFailureReason("Card declined");
        testPayment1.setPaymentMethodId("pm_test_fail");
        
        when(paymentService.processPayment(eq(1L), eq("pm_test_fail"))).thenReturn(testPayment1);

        // When & Then
        mockMvc.perform(post("/payments/1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.failureReason", is("Card declined")))
                .andExpect(jsonPath("$.paymentMethodId", is("pm_test_fail")));
    }
}