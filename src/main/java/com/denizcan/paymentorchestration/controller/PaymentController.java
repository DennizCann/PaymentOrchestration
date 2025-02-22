package com.denizcan.paymentorchestration.controller;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.service.PaymentService;
import com.denizcan.paymentorchestration.dto.PaymentRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        Payment newPayment = paymentService.createPayment(paymentRequest);
        return new ResponseEntity<>(newPayment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable String id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable String id,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        Payment payment = Payment.builder()
            .amount(paymentRequest.getAmount())
            .currency(paymentRequest.getCurrency())
            .provider(paymentRequest.getProvider())
            .description(paymentRequest.getDescription())
            .build();
        Payment updatedPayment = paymentService.updatePayment(id, payment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<Payment> completePayment(@PathVariable String id) {
        Payment payment = paymentService.completePayment(id);
        return ResponseEntity.ok(payment);
    }
    
    @PostMapping("/{id}/fail")
    public ResponseEntity<Payment> failPayment(@PathVariable String id) {
        Payment payment = paymentService.failPayment(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(@PathVariable String id) {
        Payment payment = paymentService.processPayment(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/process-async")
    public CompletableFuture<ResponseEntity<Payment>> processPaymentAsync(@PathVariable String id) {
        return paymentService.processPaymentAsynchronously(id)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
} 