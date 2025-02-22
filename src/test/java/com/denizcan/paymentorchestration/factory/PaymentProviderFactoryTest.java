package com.denizcan.paymentorchestration.factory;

import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.service.provider.ParamPaymentService;
import com.denizcan.paymentorchestration.service.provider.PaparaPaymentService;
import com.denizcan.paymentorchestration.service.provider.PaymentProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentProviderFactoryTest {

    @Mock
    private ParamPaymentService paramPaymentService;

    @Mock
    private PaparaPaymentService paparaPaymentService;

    private PaymentProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PaymentProviderFactory(Arrays.asList(paramPaymentService, paparaPaymentService));
    }

    @Test
    void getProvider_ParamProvider_ReturnsParamService() {
        // Act
        PaymentProviderService provider = factory.getProvider(PaymentProvider.PARAM);

        // Assert
        assertNotNull(provider);
        assertTrue(provider instanceof ParamPaymentService);
    }

    @Test
    void getProvider_PaparaProvider_ReturnsPaparaService() {
        // Act
        PaymentProviderService provider = factory.getProvider(PaymentProvider.PAPARA);

        // Assert
        assertNotNull(provider);
        assertTrue(provider instanceof PaparaPaymentService);
    }

    @Test
    void getProvider_UnsupportedProvider_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> factory.getProvider(PaymentProvider.ZIP));
    }

    @Test
    void constructor_WithValidProviders_InitializesSuccessfully() {
        // Act
        PaymentProviderFactory newFactory = new PaymentProviderFactory(
            Arrays.asList(paramPaymentService, paparaPaymentService)
        );

        // Assert
        assertNotNull(newFactory);
    }

    @Test
    void constructor_WithInvalidProvider_ThrowsException() {
        // Arrange
        PaymentProviderService invalidProvider = new PaymentProviderService() {
            @Override
            public boolean processPayment(com.denizcan.paymentorchestration.model.Payment payment) {
                return false;
            }

            @Override
            public boolean refundPayment(com.denizcan.paymentorchestration.model.Payment payment) {
                return false;
            }
        };

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            new PaymentProviderFactory(Arrays.asList(invalidProvider))
        );
    }
} 