package snpsu.mca.javaFS.TripNest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.entity.User;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find by status
    List<Booking> findByStatus(String status);
    
    // Find by customer
    List<Booking> findByCustomer(User customer);
    
    // Find by driver
    List<Booking> findByDriver(User driver);
    
    // Find by driver and status
    List<Booking> findByDriverAndStatus(User driver, String status);
    
    // Find by customer and status
    List<Booking> findByCustomerAndStatus(User customer, String status);
    
    // Count by status
    long countByStatus(String status);
}