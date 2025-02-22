package com.denizcan.paymentorchestration.service.provider;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ParamPaymentServiceTest {

    @InjectMocks
    private ParamPaymentService paramPaymentService;

    @Test
    void processPayment_Success_ReturnsTrue() {
        // Arrange
        Payment payment = Payment.builder()
            .id("test-id")
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.PENDING)
            .build();

        // Act
        boolean result = paramPaymentService.processPayment(payment);

        // Assert
        assertTrue(result);
    }

    @Test
    void refundPayment_Success_ReturnsTrue() {
        // Arrange
        Payment payment = Payment.builder()
            .id("test-id")
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.COMPLETED)
            .build();

        // Act
        boolean result = paramPaymentService.refundPayment(payment);

        // Assert
        assertTrue(result);
    }
} 