package com.denizcan.paymentorchestration.factory;

import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.service.provider.PaymentProviderService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentProviderFactory {
    private final Map<PaymentProvider, PaymentProviderService> providers;

    public PaymentProviderFactory(List<PaymentProviderService> providerServices) {
        providers = new HashMap<>();
        for (PaymentProviderService service : providerServices) {
            PaymentProvider providerType = getProviderType(service);
            providers.put(providerType, service);
            log.info("Ödeme sağlayıcı kaydedildi: {}", providerType);
        }
    }

    private PaymentProvider getProviderType(PaymentProviderService service) {
        String className = service.getClass().getSimpleName().toUpperCase();
        if (className.contains("PARAM")) {
            return PaymentProvider.PARAM;
        } else if (className.contains("PAPARA")) {
            return PaymentProvider.PAPARA;
        }
        // Diğer provider'lar için kontroller eklenecek
        throw new IllegalArgumentException("Desteklenmeyen ödeme sağlayıcı: " + className);
    }

    public PaymentProviderService getProvider(PaymentProvider provider) {
        PaymentProviderService service = providers.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("Ödeme sağlayıcı bulunamadı: " + provider);
        }
        return service;
    }
} 