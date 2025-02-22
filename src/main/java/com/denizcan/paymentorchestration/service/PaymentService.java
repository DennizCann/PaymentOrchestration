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

@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    // Create
    public Payment createPayment(Payment payment) {
        validatePayment(payment);
        payment.setStatus("PENDING");
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
        payment.setStatus("COMPLETED");
        return paymentRepository.save(payment);
    }
    
    public Payment failPayment(String id) {
        Payment payment = getPaymentById(id);
        payment.setStatus("FAILED");
        return paymentRepository.save(payment);
    }
} 