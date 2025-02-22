package com.denizcan.paymentorchestration.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WebhookRequest {
    private String paymentId;
    private String status;
    private String providerTransactionId;
    private BigDecimal amount;
    private String currency;
    private String errorCode;
    private String errorMessage;
} 