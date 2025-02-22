package com.denizcan.paymentorchestration.service;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.repository.PaymentRepository;
import com.denizcan.paymentorchestration.exception.PaymentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    // Create
    public Payment createPayment(Payment payment) {
        payment.setStatus("PENDING");
        return paymentRepository.save(payment);
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