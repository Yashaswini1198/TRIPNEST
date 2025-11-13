package snpsu.mca.javaFS.TripNest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.entity.Payment;
import snpsu.mca.javaFS.TripNest.entity.PaymentStatus;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.service.BookingService;
import snpsu.mca.javaFS.TripNest.service.PaymentService;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private BookingService bookingService;
    
    // Show payment form for a booking
    @GetMapping("/create/{bookingId}")
    public String showPaymentForm(@PathVariable Long bookingId, 
                                 HttpSession session, 
                                 Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            model.addAttribute("error", "Booking not found");
            return "error";
        }
        
        Booking booking = bookingOpt.get();
        
        // Check if user has permission to pay for this booking
        if (!booking.getCustomer().getId().equals(user.getId())) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
        
        // Check if payment already exists
        Optional<Payment> existingPayment = paymentService.getPaymentByBookingId(bookingId);
        if (existingPayment.isPresent()) {
            model.addAttribute("payment", existingPayment.get());
            return "payment-details";
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("payment", new Payment());
        return "payment-form";
    }
    
    // Process payment
    @PostMapping("/process/{bookingId}")
    public String processPayment(@PathVariable Long bookingId,
                                @RequestParam String paymentMethod,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
            if (bookingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/bookings/all";
            }
            
            Booking booking = bookingOpt.get();
            
            // Create payment
            Payment payment = paymentService.createPayment(booking, booking.getPrice(), paymentMethod);
            
            // Process payment
            Payment processedPayment = paymentService.processPayment(payment.getId());
            
            // Update booking status
            bookingService.updateBookingStatus(bookingId, "CONFIRMED");
            
            redirectAttributes.addFlashAttribute("success", "Payment completed successfully!");
            return "redirect:/payments/" + processedPayment.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment failed: " + e.getMessage());
            return "redirect:/payments/create/" + bookingId;
        }
    }
    
    // View payment details
    @GetMapping("/{id}")
    public String viewPayment(@PathVariable Long id, 
                            HttpSession session, 
                            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
        if (paymentOpt.isEmpty()) {
            model.addAttribute("error", "Payment not found");
            return "error";
        }
        
        Payment payment = paymentOpt.get();
        
        // Check if user has permission to view this payment
        if (!payment.getBooking().getCustomer().getId().equals(user.getId())) {
            model.addAttribute("error", "Access denied");
            return "error";
        }
        
        model.addAttribute("payment", payment);
        return "payment-details";
    }
    
    // Admin: View all payments
    @GetMapping("/admin/all")
    public String viewAllPayments(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        // FIXED: Removed .name() from String comparison
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/auth/login";
        }
        
        var payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        return "admin-payments";
    }
    
    // Admin: Update payment status
    @PostMapping("/admin/{id}/status")
    public String updatePaymentStatus(@PathVariable Long id,
                                    @RequestParam PaymentStatus status,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        // FIXED: Removed .name() from String comparison
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            paymentService.updatePaymentStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Payment status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating payment status: " + e.getMessage());
        }
        
        return "redirect:/payments/admin/all";
    }
}