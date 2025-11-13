package snpsu.mca.javaFS.TripNest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.repository.BookingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    // ========== CRUD OPERATIONS ==========
    
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
    
    // ========== ADD THE MISSING METHOD ==========
    
    public Booking updateBooking(Booking booking) {
        if (booking == null || booking.getId() == null) {
            throw new IllegalArgumentException("Booking or booking ID cannot be null");
        }
        
        // Check if booking exists
        Optional<Booking> existingBooking = bookingRepository.findById(booking.getId());
        if (existingBooking.isEmpty()) {
            throw new RuntimeException("Booking not found with id: " + booking.getId());
        }
        
        return bookingRepository.save(booking);
    }
    
    // ========== QUERY METHODS ==========
    
    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus("PENDING");
    }
    
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }
    
    public List<Booking> getUserBookings(User customer) {
        return bookingRepository.findByCustomer(customer);
    }
    
    public List<Booking> getDriverBookings(User driver) {
        return bookingRepository.findByDriver(driver);
    }
    
    public List<Booking> getDriverPendingBookings(User driver) {
        return bookingRepository.findByDriverAndStatus(driver, "PENDING");
    }
    
    public List<Booking> getUserActiveBookings(User customer) {
        return bookingRepository.findByCustomerAndStatus(customer, "CONFIRMED");
    }
    
    // ========== BUSINESS LOGIC METHODS ==========
    
    public Booking createBooking(Booking booking, User customer) {
        booking.setCustomer(customer);
        booking.setStatus("PENDING");
        return bookingRepository.save(booking);
    }
    
    public Booking assignDriverToBooking(Long bookingId, User driver) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setDriver(driver);
            booking.setStatus("ASSIGNED");
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Booking not found");
    }
    
    public Booking updateBookingStatus(Long bookingId, String status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(status);
            return bookingRepository.save(booking);
        }
        throw new RuntimeException("Booking not found");
    }
    
    // Statistics
    public long getTotalBookingsCount() {
        return bookingRepository.count();
    }
    
    public long getBookingsCountByStatus(String status) {
        return bookingRepository.findByStatus(status).size();
    }
}