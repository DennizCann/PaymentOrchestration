package com.denizcan.paymentorchestration.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    
    @Pattern(regexp = "^(PENDING|COMPLETED|FAILED)$", 
            message = "Durum sadece PENDING, COMPLETED veya FAILED olabilir")
    private String status = "PENDING";
    
    @Size(max = 255, message = "Açıklama 255 karakterden uzun olamaz")
    private String description;
} 