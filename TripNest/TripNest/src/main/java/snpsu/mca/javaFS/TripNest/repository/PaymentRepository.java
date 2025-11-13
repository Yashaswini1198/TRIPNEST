package snpsu.mca.javaFS.TripNest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import snpsu.mca.javaFS.TripNest.entity.Payment;
import snpsu.mca.javaFS.TripNest.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByBookingId(Long bookingId);
    List<Payment> findByTransactionId(String transactionId);
    List<Payment> findByPaymentMethod(String paymentMethod);
}