package com.denizcan.paymentorchestration.service.provider;

import com.denizcan.paymentorchestration.model.Payment;

public interface PaymentProviderService {
    boolean processPayment(Payment payment);
    boolean refundPayment(Payment payment);
} 