package snpsu.mca.javaFS.TripNest.controller;

import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @GetMapping("/all")
    public String getAllBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";
        
        List<Booking> bookings;
        // Using String comparison for role
        if ("DRIVER".equals(user.getRole())) {
            bookings = bookingService.getDriverBookings(user);
        } else {
            bookings = bookingService.getUserBookings(user);
        }
        
        model.addAttribute("bookings", bookings);
        return "booking-list";
    }
    
    @GetMapping("/{id}")
    public String getBookingDetails(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";
        
        Booking booking = bookingService.getBookingById(id).orElse(null);
        if (booking == null) {
            model.addAttribute("error", "Booking not found");
            return "error";
        }
        
        model.addAttribute("booking", booking);
        return "booking-details";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";
        
        try {
            bookingService.deleteBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        
        return "redirect:/bookings/all";
    }
}