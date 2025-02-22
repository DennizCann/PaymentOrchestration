package com.denizcan.paymentorchestration.service;

import com.denizcan.paymentorchestration.model.Payment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AsyncPaymentService {
    @Async("asyncExecutor")
    public CompletableFuture<Payment> processPaymentAsync(Payment payment) {
        log.info("Asenkron ödeme işlemi başlatıldı: {}", payment.getId());
        try {
            Thread.sleep(2000); // Simüle edilmiş uzun süren işlem
            return CompletableFuture.completedFuture(payment);
        } catch (InterruptedException e) {
            log.error("Asenkron işlem başarısız: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }
} 