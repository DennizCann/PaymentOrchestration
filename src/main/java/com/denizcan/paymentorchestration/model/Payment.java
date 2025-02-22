package com.denizcan.paymentorchestration.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotNull(message = "Tutar boş olamaz")
    @Positive(message = "Tutar pozitif olmalıdır")
    private BigDecimal amount;
    
    @NotBlank(message = "Para birimi boş olamaz")
    @Size(min = 3, max = 3, message = "Para birimi 3 karakter olmalıdır (Örn: TRY, USD)")
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;
    
    @Size(max = 255, message = "Açıklama 255 karakterden uzun olamaz")
    private String description;
} 