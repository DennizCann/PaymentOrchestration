package com.denizcan.paymentorchestration.service;

import com.denizcan.paymentorchestration.model.Payment;
import com.denizcan.paymentorchestration.model.PaymentProvider;
import com.denizcan.paymentorchestration.model.PaymentStatus;
import com.denizcan.paymentorchestration.repository.PaymentRepository;
import com.denizcan.paymentorchestration.dto.PaymentRequest;
import com.denizcan.paymentorchestration.exception.PaymentNotFoundException;
import com.denizcan.paymentorchestration.exception.PaymentValidationException;
import com.denizcan.paymentorchestration.factory.PaymentProviderFactory;
import com.denizcan.paymentorchestration.service.provider.PaymentProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProviderFactory providerFactory;

    @Mock
    private AsyncPaymentService asyncPaymentService;

    @Mock
    private PaymentProviderService paymentProviderService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, providerFactory, asyncPaymentService);
    }

    @Test
    void createPayment_ValidRequest_ReturnsPayment() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .description("Test ödeme")
            .build();

        Payment expectedPayment = Payment.builder()
            .id("test-id")
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .provider(request.getProvider())
            .description(request.getDescription())
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        // Act
        Payment result = paymentService.createPayment(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedPayment.getAmount(), result.getAmount());
        assertEquals(expectedPayment.getCurrency(), result.getCurrency());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPayment_InvalidAmount_ThrowsException() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("-100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .build();

        // Act & Assert
        assertThrows(PaymentValidationException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_InvalidCurrency_ThrowsException() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("INVALID")  // Geçersiz para birimi
            .provider(PaymentProvider.PARAM)
            .build();

        // Act & Assert
        assertThrows(PaymentValidationException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void createPayment_NullProvider_ThrowsException() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(null)  // Sağlayıcı boş
            .build();

        // Act & Assert
        assertThrows(PaymentValidationException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_Success_ReturnsCompletedPayment() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(providerFactory.getProvider(any())).thenReturn(paymentProviderService);
        when(paymentProviderService.processPayment(any())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentService.processPayment(paymentId);

        // Assert
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        verify(paymentProviderService).processPayment(any(Payment.class));
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void processPayment_Failed_ReturnsFailedPayment() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(providerFactory.getProvider(any())).thenReturn(paymentProviderService);
        when(paymentProviderService.processPayment(any())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentService.processPayment(paymentId);

        // Assert
        assertEquals(PaymentStatus.FAILED, result.getStatus());
    }

    @Test
    void processPayment_NotFound_ThrowsException() {
        // Arrange
        String paymentId = "non-existent-id";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class, () -> paymentService.processPayment(paymentId));
    }

    @Test
    void processPayment_AlreadyCompleted_ThrowsException() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.COMPLETED)  // Zaten tamamlanmış ödeme
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(PaymentValidationException.class, () -> paymentService.processPayment(paymentId));
        verify(paymentProviderService, never()).processPayment(any(Payment.class));
    }

    @Test
    void processPaymentAsync_Success_ReturnsCompletedFuture() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.PENDING)
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(asyncPaymentService.processPaymentAsync(any()))
            .thenReturn(CompletableFuture.completedFuture(payment));
        when(providerFactory.getProvider(any())).thenReturn(paymentProviderService);
        when(paymentProviderService.processPayment(any())).thenReturn(true);

        // Act
        CompletableFuture<Payment> future = paymentService.processPaymentAsynchronously(paymentId);

        // Assert
        assertNotNull(future);
        assertDoesNotThrow(() -> future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void updatePayment_Success_ReturnsUpdatedPayment() {
        // Arrange
        String paymentId = "test-id";
        Payment existingPayment = Payment.builder()
            .id(paymentId)
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .status(PaymentStatus.PENDING)
            .build();

        Payment updateDetails = Payment.builder()
            .amount(new BigDecimal("150.00"))
            .currency("USD")
            .description("Updated description")
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(existingPayment);

        // Act
        Payment result = paymentService.updatePayment(paymentId, updateDetails);

        // Assert
        assertEquals(updateDetails.getAmount(), result.getAmount());
        assertEquals(updateDetails.getCurrency(), result.getCurrency());
        assertEquals(updateDetails.getDescription(), result.getDescription());
        assertEquals(PaymentStatus.PENDING, result.getStatus()); // Status değişmemeli
    }

    @Test
    void refundPayment_Success_ReturnsRefundedPayment() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .amount(new BigDecimal("100.00"))
            .currency("TRY")
            .provider(PaymentProvider.PARAM)
            .status(PaymentStatus.COMPLETED)  // Sadece tamamlanmış ödemeler iade edilebilir
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(providerFactory.getProvider(any())).thenReturn(paymentProviderService);
        when(paymentProviderService.refundPayment(any())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentService.refundPayment(paymentId);

        // Assert
        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        verify(paymentProviderService).refundPayment(any(Payment.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void refundPayment_NotCompleted_ThrowsException() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.PENDING)  // Tamamlanmamış ödeme
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(PaymentValidationException.class, 
            () -> paymentService.refundPayment(paymentId));
        verify(paymentProviderService, never()).refundPayment(any(Payment.class));
    }

    @Test
    void refundPayment_AlreadyRefunded_ThrowsException() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.REFUNDED)  // Zaten iade edilmiş
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThrows(PaymentValidationException.class, 
            () -> paymentService.refundPayment(paymentId));
        verify(paymentProviderService, never()).refundPayment(any(Payment.class));
    }

    @Test
    void refundPayment_ProviderFailure_ThrowsException() {
        // Arrange
        String paymentId = "test-id";
        Payment payment = Payment.builder()
            .id(paymentId)
            .status(PaymentStatus.COMPLETED)
            .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(providerFactory.getProvider(any())).thenReturn(paymentProviderService);
        when(paymentProviderService.refundPayment(any())).thenReturn(false);

        // Act & Assert
        assertThrows(PaymentValidationException.class, 
            () -> paymentService.refundPayment(paymentId));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}