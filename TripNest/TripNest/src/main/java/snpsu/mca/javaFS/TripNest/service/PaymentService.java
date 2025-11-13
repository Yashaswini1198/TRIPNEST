package snpsu.mca.javaFS.TripNest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.entity.Payment;
import snpsu.mca.javaFS.TripNest.entity.PaymentStatus;
import snpsu.mca.javaFS.TripNest.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Create a new payment
    public Payment createPayment(Booking booking, Double amount, String paymentMethod) {
        Payment payment = new Payment(booking, amount, paymentMethod);
        payment.setTransactionId(generateTransactionId());
        return paymentRepository.save(payment);
    }
    
    // Get payment by ID
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    // Get payment by booking ID
    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
    
    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    // Get payments by status
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    // Update payment status
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(status);
            if (status == PaymentStatus.COMPLETED) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Payment not found");
    }
    
    // Process payment (simulate payment processing)
    public Payment processPayment(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            // Simulate payment processing
            // In real application, this would integrate with payment gateway
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());
            
            return paymentRepository.save(payment);
        }
        throw new RuntimeException("Payment not found");
    }
    
    // Generate unique transaction ID
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Delete payment
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}