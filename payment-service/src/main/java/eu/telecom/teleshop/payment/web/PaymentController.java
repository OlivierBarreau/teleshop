package eu.telecom.teleshop.payment.web;

import eu.telecom.teleshop.payment.model.Payment;
import eu.telecom.teleshop.payment.repository.PaymentRepository;
import eu.telecom.teleshop.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    
    public PaymentController(PaymentService paymentService, PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }
    
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }
    
    @GetMapping("/order/{orderId}")
    public Payment getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }
    
    @PostMapping("/{id}/process")
    public Payment processPayment(@PathVariable Long id, @RequestBody ProcessPaymentRequest request) {
        return paymentService.processPayment(id, request.getPaymentMethodId());
    }
}
