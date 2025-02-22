package com.denizcan.paymentorchestration.service;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.repository.PaymentRepository;
import com.denizcan.paymentorchestration.exception.PaymentNotFoundException;
import com.denizcan.paymentorchestration.exception.PaymentValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import com.denizcan.paymentorchestration.dto.PaymentRequest;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import com.denizcan.paymentorchestration.factory.PaymentProviderFactory;
import com.denizcan.paymentorchestration.service.provider.PaymentProviderService;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentProviderFactory providerFactory;
    private final AsyncPaymentService asyncPaymentService;

    @Autowired
    public PaymentService(
            PaymentRepository paymentRepository, 
            PaymentProviderFactory providerFactory,
            AsyncPaymentService asyncPaymentService) {
        this.paymentRepository = paymentRepository;
        this.providerFactory = providerFactory;
        this.asyncPaymentService = asyncPaymentService;
    }
    
    // Create
    public Payment createPayment(PaymentRequest request) {
        Payment payment = Payment.builder()
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .provider(request.getProvider())
            .description(request.getDescription())
            .status(PaymentStatus.PENDING)
            .build();
            
        validatePayment(payment);
        return paymentRepository.save(payment);
    }

    private void validatePayment(Payment payment) {
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentValidationException("Ödeme tutarı sıfırdan büyük olmalıdır");
        }
        
        if (payment.getCurrency() == null || payment.getCurrency().trim().isEmpty()) {
            throw new PaymentValidationException("Para birimi belirtilmelidir");
        }
        
        // Desteklenen para birimlerini kontrol et
        List<String> supportedCurrencies = Arrays.asList("TRY", "USD", "EUR");
        if (!supportedCurrencies.contains(payment.getCurrency())) {
            throw new PaymentValidationException("Desteklenmeyen para birimi: " + payment.getCurrency());
        }
        
        if (payment.getProvider() == null) {
            throw new PaymentValidationException("Ödeme sağlayıcı seçilmelidir");
        }
    }

    // Read
    public Payment getPaymentById(String id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new PaymentNotFoundException("Ödeme bulunamadı: " + id));
    }
    
    // Read All
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    // Update
    public Payment updatePayment(String id, Payment paymentDetails) {
        Payment payment = getPaymentById(id); // Bu metod zaten PaymentNotFoundException fırlatıyor
        
        payment.setAmount(paymentDetails.getAmount());
        payment.setCurrency(paymentDetails.getCurrency());
        payment.setDescription(paymentDetails.getDescription());
        // Status'u direkt güncellemeyelim, bu iş mantığına göre ayrı bir metod ile yapılmalı
        
        return paymentRepository.save(payment);
    }
    
    // Delete
    public void deletePayment(String id) {
        Payment payment = getPaymentById(id); // Önce varlığını kontrol edelim
        paymentRepository.delete(payment);
    }
    
    // İş mantığı metodları
    public Payment completePayment(String id) {
        Payment payment = getPaymentById(id);
        validateStatusTransition(payment, PaymentStatus.COMPLETED);
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }
    
    public Payment failPayment(String id) {
        Payment payment = getPaymentById(id);
        validateStatusTransition(payment, PaymentStatus.FAILED);
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    private void validateStatusTransition(Payment payment, PaymentStatus newStatus) {
        PaymentStatus currentStatus = payment.getStatus();
        
        // Örnek kural: COMPLETED veya FAILED durumundaki ödemeler değiştirilemez
        if (currentStatus == PaymentStatus.COMPLETED || currentStatus == PaymentStatus.FAILED) {
            throw new PaymentValidationException(
                String.format("'%s' durumundaki ödemenin durumu değiştirilemez", currentStatus));
        }
        
        // Örnek kural: PROCESSING durumundan sadece COMPLETED veya FAILED'a geçilebilir
        if (currentStatus == PaymentStatus.PROCESSING && 
            newStatus != PaymentStatus.COMPLETED && 
            newStatus != PaymentStatus.FAILED) {
            throw new PaymentValidationException(
                "İşlemdeki ödeme sadece tamamlanabilir veya iptal edilebilir");
        }
    }

    public Payment processPayment(String id) {
        Payment payment = getPaymentById(id);
        validateStatusTransition(payment, PaymentStatus.PROCESSING);
        
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        
        PaymentProviderService provider = providerFactory.getProvider(payment.getProvider());
        boolean success = provider.processPayment(payment);
        
        if (success) {
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        
        return paymentRepository.save(payment);
    }

    public CompletableFuture<Payment> processPaymentAsynchronously(String id) {
        Payment payment = getPaymentById(id);
        validateStatusTransition(payment, PaymentStatus.PROCESSING);
        
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        
        return asyncPaymentService.processPaymentAsync(payment)
            .thenApply(processedPayment -> {
                PaymentProviderService provider = providerFactory.getProvider(payment.getProvider());
                boolean success = provider.processPayment(processedPayment);
                
                if (success) {
                    processedPayment.setStatus(PaymentStatus.COMPLETED);
                } else {
                    processedPayment.setStatus(PaymentStatus.FAILED);
                }
                
                return paymentRepository.save(processedPayment);
            })
            .exceptionally(ex -> {
                log.error("Ödeme işlemi başarısız: {}", ex.getMessage());
                payment.setStatus(PaymentStatus.FAILED);
                return paymentRepository.save(payment);
            });
    }
} 