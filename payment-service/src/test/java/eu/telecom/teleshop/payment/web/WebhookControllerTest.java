package eu.telecom.teleshop.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.telecom.teleshop.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for WebhookController - Tests webhook payload processing from external systems
 * 
 * Exercise 11.2: How would you test the webhook endpoint? 
 * Answer: We use MockMvc to simulate webhook POST requests from external systems (MockPayments),
 * test various payload structures, verify PaymentService is called with correct parameters,
 * and ensure proper HTTP responses are returned for success and error cases.
 */
@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> webhookPayload;

    @BeforeEach
    void setUp() {
        webhookPayload = new HashMap<>();
    }

    /**
     * Test successful webhook processing for payment_intent.succeeded event
     */
    @Test
    void testHandleWebhook_PaymentSucceeded() throws Exception {
        // Given - Simulate MockPayments webhook payload for successful payment
        String paymentIntentId = "pi_test_success_123";
        webhookPayload = createWebhookPayload("payment_intent.succeeded", paymentIntentId);

        // PaymentService should handle the webhook without throwing exception
        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.succeeded"), eq(paymentIntentId));

        // When & Then
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        // Verify PaymentService was called with correct parameters
        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.succeeded", paymentIntentId);
    }

    /**
     * Test webhook processing for payment_intent.payment_failed event
     */
    @Test
    void testHandleWebhook_PaymentFailed() throws Exception {
        // Given - Simulate MockPayments webhook payload for failed payment
        String paymentIntentId = "pi_test_failed_456";
        webhookPayload = createWebhookPayload("payment_intent.payment_failed", paymentIntentId);

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.payment_failed"), eq(paymentIntentId));

        // When & Then
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.payment_failed", paymentIntentId);
    }

    /**
     * Test webhook processing for payment_intent.requires_action event
     */
    @Test
    void testHandleWebhook_RequiresAction() throws Exception {
        // Given - Simulate webhook for payment requiring additional action (e.g., 3D Secure)
        String paymentIntentId = "pi_test_action_789";
        webhookPayload = createWebhookPayload("payment_intent.requires_action", paymentIntentId);

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.requires_action"), eq(paymentIntentId));

        // When & Then
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.requires_action", paymentIntentId);
    }

    /**
     * Test webhook processing for payment_intent.canceled event
     */
    @Test
    void testHandleWebhook_PaymentCanceled() throws Exception {
        // Given
        String paymentIntentId = "pi_test_canceled_101";
        webhookPayload = createWebhookPayload("payment_intent.canceled", paymentIntentId);

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.canceled"), eq(paymentIntentId));

        // When & Then
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.canceled", paymentIntentId);
    }

    /**
     * Test webhook with processing error in PaymentService
     * Exercise 11.1 consideration: When external systems are unavailable, 
     * we should return appropriate error status to allow webhook retries
     */
    @Test
    void testHandleWebhook_ServiceError() throws Exception {
        // Given - PaymentService throws exception (simulating unavailable MockPayments API)
        String paymentIntentId = "pi_test_error_999";
        webhookPayload = createWebhookPayload("payment_intent.succeeded", paymentIntentId);

        doThrow(new RuntimeException("Payment service unavailable"))
                .when(paymentService).handleWebhookEvent(eq("payment_intent.succeeded"), eq(paymentIntentId));

        // When & Then - Should return 500 error so external system can retry
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Webhook processing failed"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.succeeded", paymentIntentId);
    }

    /**
     * Test webhook with malformed payload (missing required fields)
     */
    @Test
    void testHandleWebhook_MalformedPayload_MissingData() throws Exception {
        // Given - Malformed payload without 'data' field
        webhookPayload.put("type", "payment_intent.succeeded");
        // Missing 'data' field

        // When & Then - Should handle gracefully and return error
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Webhook processing failed"));

        // PaymentService should not be called due to NullPointerException
        verify(paymentService, never()).handleWebhookEvent(any(), any());
    }

    /**
     * Test webhook with empty payment intent ID
     */
    @Test
    void testHandleWebhook_EmptyPaymentIntentId() throws Exception {
        // Given - Payload with empty payment intent ID
        webhookPayload = createWebhookPayload("payment_intent.succeeded", "");

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.succeeded"), eq(""));

        // When & Then - Controller should still accept and pass to service (service handles validation)
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.succeeded", "");
    }

    /**
     * Test webhook with unknown event type
     */
    @Test
    void testHandleWebhook_UnknownEventType() throws Exception {
        // Given - Unknown event type from external system
        String paymentIntentId = "pi_test_unknown_202";
        webhookPayload = createWebhookPayload("payment_intent.unknown_event", paymentIntentId);

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.unknown_event"), eq(paymentIntentId));

        // When & Then - Should still accept (service layer handles event type filtering)
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.unknown_event", paymentIntentId);
    }

    /**
     * Test webhook with complex nested payload structure
     */
    @Test
    void testHandleWebhook_ComplexPayload() throws Exception {
        // Given - More realistic webhook payload with additional fields
        String paymentIntentId = "pi_test_complex_303";
        Map<String, Object> complexPayload = createComplexWebhookPayload(
                "payment_intent.succeeded", 
                paymentIntentId,
                2000,
                "EUR",
                "SUCCEEDED"
        );

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.succeeded"), eq(paymentIntentId));

        // When & Then
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(complexPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook received"));

        verify(paymentService, times(1)).handleWebhookEvent("payment_intent.succeeded", paymentIntentId);
    }

    /**
     * Test concurrent webhook processing (simulate race condition)
     */
    @Test
    void testHandleWebhook_ConcurrentProcessing() throws Exception {
        // Given - Same payment intent receives multiple webhooks
        String paymentIntentId = "pi_test_concurrent_404";
        webhookPayload = createWebhookPayload("payment_intent.processing", paymentIntentId);

        doNothing().when(paymentService).handleWebhookEvent(eq("payment_intent.processing"), eq(paymentIntentId));

        // When & Then - Both webhooks should be accepted
        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/webhooks/mockpayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookPayload)))
                .andExpect(status().isOk());

        // PaymentService should be called twice (idempotency handled at service layer)
        verify(paymentService, times(2)).handleWebhookEvent("payment_intent.processing", paymentIntentId);
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a standard webhook payload structure matching MockPayments format
     */
    private Map<String, Object> createWebhookPayload(String eventType, String paymentIntentId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        object.put("id", paymentIntentId);
        data.put("object", object);
        payload.put("data", data);

        return payload;
    }

    /**
     * Creates a complex webhook payload with additional payment details
     */
    private Map<String, Object> createComplexWebhookPayload(
            String eventType, 
            String paymentIntentId,
            int amount,
            String currency,
            String status
    ) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);
        payload.put("created", System.currentTimeMillis() / 1000);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> object = new HashMap<>();
        object.put("id", paymentIntentId);
        object.put("amount", amount);
        object.put("currency", currency);
        object.put("status", status);
        object.put("client_secret", paymentIntentId + "_secret_abc123");
        
        data.put("object", object);
        payload.put("data", data);

        return payload;
    }
}
