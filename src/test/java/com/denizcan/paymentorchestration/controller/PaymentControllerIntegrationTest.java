package com.denizcan.paymentorchestration.controller;

import com.denizcan.paymentorchestration.dto.PaymentRequest;
import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import com.denizcan.paymentorchestration.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({PaymentService.class})
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @Test
    void createPayment_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .description("Test ödeme")
            .build();

        Payment expectedPayment = Payment.builder()
            .id("test-id")
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .provider(request.getProvider())
            .description(request.getDescription())
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(expectedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(expectedPayment.getId()))
            .andExpect(jsonPath("$.amount").value(expectedPayment.getAmount().doubleValue()))
            .andExpect(jsonPath("$.status").value(expectedPayment.getStatus().toString()));
    }

    @Test
    void processPayment_ValidId_ReturnsOk() throws Exception {
        // Arrange
        String paymentId = "test-id";
        Payment processedPayment = Payment.builder()
            .id(paymentId)
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.COMPLETED)
            .build();

        when(paymentService.processPayment(paymentId)).thenReturn(processedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/payments/{id}/process", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(paymentId))
            .andExpect(jsonPath("$.status").value(PaymentStatus.COMPLETED.toString()));
    }

    @Test
    void processPaymentAsync_ValidId_ReturnsOk() throws Exception {
        // Arrange
        String paymentId = "test-id";
        Payment processedPayment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.PROCESSING)
            .build();

        when(paymentService.processPaymentAsynchronously(paymentId))
            .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(processedPayment));

        // Act & Assert
        mockMvc.perform(post("/api/payments/{id}/process-async", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(paymentId))
            .andExpect(jsonPath("$.status").value(PaymentStatus.PROCESSING.toString()));
    }
} 