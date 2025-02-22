package com.denizcan.paymentorchestration.controller;

import com.denizcan.paymentorchestration.dto.WebhookRequest;
import com.denizcan.paymentorchestration.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    private final PaymentService paymentService;
    
    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment-result")
    public ResponseEntity<Void> handlePaymentResult(@RequestBody WebhookRequest request) {
        log.info("Ödeme sonucu webhook alındı: {}", request);
        
        if ("SUCCESS".equals(request.getStatus())) {
            paymentService.completePayment(request.getPaymentId());
        } else {
            paymentService.failPayment(request.getPaymentId());
        }
        
        return ResponseEntity.ok().build();
    }
} 