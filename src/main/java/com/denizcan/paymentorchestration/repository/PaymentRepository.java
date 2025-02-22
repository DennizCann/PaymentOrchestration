package com.denizcan.paymentorchestration.repository;

import com.denizcan.paymentorchestration.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // JpaRepository bize temel CRUD operasyonlarını sağlar
    // Özel sorgular gerekirse buraya ekleyebiliriz
} 