package com.denizcan.paymentorchestration.service.provider;

import com.denizcan.paymentorchestration.model.Payment;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ParamPaymentService implements PaymentProviderService {
    @Override
    public boolean processPayment(Payment payment) {
        log.info("PARAM ile ödeme işlemi başlatıldı: {}", payment.getId());
        // Burada PARAM API entegrasyonu yapılacak
        try {
            Thread.sleep(1000); // Simüle edilmiş API çağrısı
            return true;
        } catch (InterruptedException e) {
            log.error("PARAM ödeme işlemi başarısız: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean refundPayment(Payment payment) {
        log.info("PARAM ile iade işlemi başlatıldı: {}", payment.getId());
        try {
            Thread.sleep(1000); // Simüle edilmiş API çağrısı
            return true;
        } catch (InterruptedException e) {
            log.error("PARAM iade işlemi başarısız: {}", e.getMessage());
            return false;
        }
    }
} 