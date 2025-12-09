package eu.telecom.teleshop.payment.client;

import eu.telecom.teleshop.payment.exception.MockPaymentsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class MockPaymentsClient {
    
    private final WebClient webClient;
    private final String apiKey;
    
    public MockPaymentsClient(WebClient.Builder webClientBuilder,
                             @Value("${mockpayments.base-url}") String baseUrl,
                             @Value("${mockpayments.api-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
        this.apiKey = apiKey;
    }
    
    public PaymentIntent createPaymentIntent(CreatePaymentIntentRequest request) {
        try {
            return webClient.post()
                    .uri("/payment_intents")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to create payment intent: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to create payment intent", e);
        }
    }
    
    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        try {
            return webClient.get()
                    .uri("/payment_intents/{id}", paymentIntentId)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new MockPaymentsException("Failed to get payment intent: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to get payment intent", e);
        }
    }
    
    public PaymentIntent attachPaymentMethod(String paymentIntentId, String paymentMethodId) {
        try {
            AttachPaymentMethodRequest request = new AttachPaymentMethodRequest(paymentMethodId);
            return webClient.post()
                    .uri("/payment_intents/{id}/attach_payment_method", paymentIntentId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to attach payment method: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to attach payment method", e);
        }
    }
    
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) {
        try {
            return webClient.post()
                    .uri("/payment_intents/{id}/confirm", paymentIntentId)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to confirm payment intent: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to confirm payment intent", e);
        }
    }
    
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) {
        try {
            return webClient.post()
                    .uri("/payment_intents/{id}/cancel", paymentIntentId)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to cancel payment intent: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to cancel payment intent", e);
        }
    }

    public PaymentIntent capturePaymentIntent(String paymentIntentId, Integer amountToCapture) {
        try {
            Object body = amountToCapture != null ? new CapturePaymentIntentRequest(amountToCapture) : new CapturePaymentIntentRequest();
            return webClient.post()
                    .uri("/payment_intents/{id}/capture", paymentIntentId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(PaymentIntent.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to capture payment intent: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to capture payment intent", e);
        }
    }

    public PaymentMethod createPaymentMethod(CreatePaymentMethodRequest request) {
        try {
            return webClient.post()
                    .uri("/payment_methods")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentMethod.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to create payment method: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to create payment method", e);
        }
    }

    public void configureWebhook(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        try {
            Map<String, String> body = Map.of("url", webhookUrl);
            webClient.post()
                    .uri("/webhooks")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new MockPaymentsException("Failed to configure webhook: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new MockPaymentsException("Failed to configure webhook", e);
        }
    }
}