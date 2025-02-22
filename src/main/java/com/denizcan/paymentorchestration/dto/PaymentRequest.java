package com.denizcan.paymentorchestration.dto;

import com.denizcan.paymentorchestration.model.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentRequest {
    @NotNull(message = "Tutar boş olamaz")
    @Positive(message = "Tutar pozitif olmalıdır")
    private BigDecimal amount;
    
    @NotBlank(message = "Para birimi boş olamaz")
    private String currency;
    
    @NotBlank(message = "Ödeme sağlayıcı seçilmelidir")
    private PaymentProvider provider;
    
    private String description;
} 