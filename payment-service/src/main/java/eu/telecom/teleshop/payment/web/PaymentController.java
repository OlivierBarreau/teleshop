package eu.telecom.teleshop.payment.web;

import eu.telecom.teleshop.payment.client.CreatePaymentMethodRequest;
import eu.telecom.teleshop.payment.client.MockPaymentsClient;
import eu.telecom.teleshop.payment.client.PaymentMethod;
import eu.telecom.teleshop.payment.exception.PaymentNotFoundException;
import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import eu.telecom.teleshop.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final MockPaymentsClient mockPaymentsClient;
    
    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository, MockPaymentsClient mockPaymentsClient) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.mockPaymentsClient = mockPaymentsClient;
    }
    
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException(id));
    }
    
    @GetMapping("/order/{orderId}")
    public Payment getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new PaymentNotFoundException("orderId", orderId));
    }
    
    @PostMapping("/{id}/process")
    public Payment processPayment(@PathVariable Long id, @RequestBody ProcessPaymentRequest request) {
        return paymentService.processPayment(id, request.getPaymentMethodId());
    }
    
    @PostMapping("/{id}/refresh")
    public Payment refreshPaymentStatus(@PathVariable Long id) {
        return paymentService.refreshPaymentStatus(id);
    }
    
    @PostMapping("/{id}/capture")
    public Payment capturePayment(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Integer amountToCapture = null;
        if (body != null && body.containsKey("amountToCapture")) {
            amountToCapture = (Integer) body.get("amountToCapture");
        }
        return paymentService.capturePayment(id, amountToCapture);
    }
    
    @PostMapping("/{id}/cancel")
    public Payment cancelPayment(@PathVariable Long id) {
        return paymentService.cancelPayment(id);
    }
    
    @PostMapping("/methods")
    public PaymentMethod createPaymentMethod(@RequestBody CreatePaymentMethodRequest request) {
        return mockPaymentsClient.createPaymentMethod(request);
    }
}
