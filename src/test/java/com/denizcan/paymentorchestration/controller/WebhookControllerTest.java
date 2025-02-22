package com.denizcan.paymentorchestration.controller;

import com.denizcan.paymentorchestration.dto.WebhookRequest;
import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import com.denizcan.paymentorchestration.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
@Import({PaymentService.class})
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @Test
    void handlePaymentResult_SuccessStatus_ReturnsOk() throws Exception {
        // Arrange
        WebhookRequest request = new WebhookRequest();
        request.setPaymentId("test-id");
        request.setStatus("SUCCESS");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("TRY");

        Payment completedPayment = Payment.builder()
            .id(request.getPaymentId())
            .status(PaymentStatus.COMPLETED)
            .build();

        when(paymentService.completePayment(anyString())).thenReturn(completedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/webhooks/payment-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void handlePaymentResult_FailureStatus_ReturnsOk() throws Exception {
        // Arrange
        WebhookRequest request = new WebhookRequest();
        request.setPaymentId("test-id");
        request.setStatus("FAILURE");
        request.setErrorCode("ERROR_001");
        request.setErrorMessage("İşlem başarısız");

        Payment failedPayment = Payment.builder()
            .id(request.getPaymentId())
            .status(PaymentStatus.FAILED)
            .build();

        when(paymentService.failPayment(anyString())).thenReturn(failedPayment);

        // Act & Assert
        mockMvc.perform(post("/api/webhooks/payment-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void handlePaymentResult_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        WebhookRequest request = new WebhookRequest();
        // Eksik veri ile request oluştur

        // Act & Assert
        mockMvc.perform(post("/api/webhooks/payment-result")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
} 