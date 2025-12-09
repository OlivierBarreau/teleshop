package eu.telecom.teleshop.payment.web;

import eu.telecom.teleshop.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final PaymentService paymentService;
    
    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @PostMapping("/mockpayments")
    public ResponseEntity<String> handleMockPaymentsWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received webhook payload: {}", payload);
            
            // Extract event type and payment intent ID from payload
            String eventType = (String) payload.get("type");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            String paymentIntentId = (String) object.get("id");
            
            log.info("Processing webhook event: {} for payment intent: {}", eventType, paymentIntentId);
            
            // Delegate to PaymentService to handle the webhook and update payment status
            paymentService.handleWebhookEvent(eventType, paymentIntentId);
            
            return ResponseEntity.ok("Webhook received");
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }
}