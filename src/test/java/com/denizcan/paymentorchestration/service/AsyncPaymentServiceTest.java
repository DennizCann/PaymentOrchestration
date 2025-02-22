package com.denizcan.paymentorchestration.service;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AsyncPaymentServiceTest {

    @InjectMocks
    private AsyncPaymentService asyncPaymentService;

    @Test
    void processPaymentAsync_Success_ReturnsCompletedFuture() throws ExecutionException, InterruptedException {
        // Arrange
        Payment payment = Payment.builder()
            .id("test-id")
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.PENDING)
            .build();

        // Act
        CompletableFuture<Payment> future = asyncPaymentService.processPaymentAsync(payment);

        // Assert
        Payment result = future.get(); // Bekleyerek sonucu al
        assertNotNull(result);
        assertEquals(payment.getId(), result.getId());
    }

    @Test
    void processPaymentAsync_WithInterruption_ReturnsFutureWithException() {
        // Arrange
        Payment payment = Payment.builder()
            .id("test-id")
            .build();

        // Act & Assert
        CompletableFuture<Payment> future = asyncPaymentService.processPaymentAsync(payment);
        Thread.currentThread().interrupt(); // Thread'i interrupt et
        
        assertThrows(ExecutionException.class, future::get);
    }
} 