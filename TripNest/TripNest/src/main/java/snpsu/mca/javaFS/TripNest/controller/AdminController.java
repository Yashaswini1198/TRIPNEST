package snpsu.mca.javaFS.TripNest.controller;

import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.entity.Booking;
import snpsu.mca.javaFS.TripNest.service.UserService;
import snpsu.mca.javaFS.TripNest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;
    
    // ========== DASHBOARD ==========
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            // Get pending drivers (drivers who are not approved)
            List<User> allUsers = userService.getAllUsers();
            List<User> pendingDrivers = allUsers.stream()
                .filter(user -> "DRIVER".equals(user.getRole()) && !Boolean.TRUE.equals(user.getApproved()))
                .toList();
                
            List<User> approvedDrivers = allUsers.stream()
                .filter(user -> "DRIVER".equals(user.getRole()) && Boolean.TRUE.equals(user.getApproved()))
                .toList();
            
            List<Booking> pendingBookings = bookingService.getPendingBookings();
            
            // Add statistics
            long totalUsers = allUsers.size();
            long totalPendingDrivers = pendingDrivers.size();
            long totalApprovedDrivers = approvedDrivers.size();
            long totalPendingBookings = pendingBookings.size();
            
            model.addAttribute("admin", admin);
            model.addAttribute("pendingDrivers", pendingDrivers);
            model.addAttribute("users", allUsers);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("approvedDrivers", approvedDrivers);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalPendingDrivers", totalPendingDrivers);
            model.addAttribute("totalApprovedDrivers", totalApprovedDrivers);
            model.addAttribute("totalPendingBookings", totalPendingBookings);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }
        
        return "admin-dashboard";
    }
    
    // ========== DRIVER MANAGEMENT ==========
    
    @PostMapping("/approve-driver/{id}")
    public String approveDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOptional = userService.findUserById(id);
            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Driver not found");
                return "redirect:/admin/dashboard";
            }
            
            User driver = userOptional.get();
            
            if (!"DRIVER".equals(driver.getRole())) {
                redirectAttributes.addFlashAttribute("error", "User is not a driver");
                return "redirect:/admin/dashboard";
            }
            
            if (Boolean.TRUE.equals(driver.getApproved())) {
                redirectAttributes.addFlashAttribute("error", "Driver is already approved");
                return "redirect:/admin/dashboard";
            }
            
            driver.setApproved(true);
            userService.updateUser(driver);
            
            redirectAttributes.addFlashAttribute("success", "Driver approved successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving driver: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
    
    @PostMapping("/reject-driver/{id}")
    public String rejectDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOptional = userService.findUserById(id);
            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Driver not found");
                return "redirect:/admin/dashboard";
            }
            
            User driver = userOptional.get();
            
            if (!"DRIVER".equals(driver.getRole())) {
                redirectAttributes.addFlashAttribute("error", "User is not a driver");
                return "redirect:/admin/dashboard";
            }
            
            // For now, just set as not approved instead of deleting
            driver.setApproved(false);
            userService.updateUser(driver);
            
            redirectAttributes.addFlashAttribute("success", "Driver rejected successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting driver: " + e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
    
    // ========== USER MANAGEMENT ==========
    
    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
        }
        
        return "admin-users";
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            User currentAdmin = (User) session.getAttribute("user");
            if (currentAdmin == null || !"ADMIN".equals(currentAdmin.getRole())) {
                return "redirect:/auth/login";
            }
            
            Optional<User> userToDeleteOptional = userService.findUserById(id);
            if (userToDeleteOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/admin/users";
            }
            
            User userToDelete = userToDeleteOptional.get();
            
            // Prevent admin from deleting themselves
            if (currentAdmin.getId().equals(userToDelete.getId())) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete your own account");
                return "redirect:/admin/users";
            }
            
            // Prevent deleting other admins
            if ("ADMIN".equals(userToDelete.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete admin user");
                return "redirect:/admin/users";
            }
            
            // For now, just mark as inactive instead of deleting
            // userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deletion functionality to be implemented");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    // ========== ADD USER FUNCTIONALITY ==========

    @GetMapping("/users/add")
    public String showAddUserForm(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", new User());
        model.addAttribute("roles", List.of("USER", "DRIVER", "ADMIN"));
        return "admin-add-user";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user,
                         @RequestParam String confirmPassword,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            // Check if passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                model.addAttribute("roles", List.of("USER", "DRIVER", "ADMIN"));
                return "admin-add-user";
            }
            
            // Auto-approve users added by admin
            user.setApproved(true);
            
            User registeredUser = userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "User added successfully!");
            
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error adding user: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("roles", List.of("USER", "DRIVER", "ADMIN"));
            return "admin-add-user";
        }
    }
    
    // ========== BOOKING MANAGEMENT ==========
    
    @GetMapping("/bookings")
    public String manageBookings(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Booking> pendingBookings = bookingService.getPendingBookings();
            
            // Filter bookings by status
            List<Booking> completedBookings = allBookings.stream()
                .filter(booking -> "COMPLETED".equals(booking.getStatus()))
                .toList();
                
            List<Booking> cancelledBookings = allBookings.stream()
                .filter(booking -> "CANCELLED".equals(booking.getStatus()))
                .toList();
            
            model.addAttribute("allBookings", allBookings);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("completedBookings", completedBookings);
            model.addAttribute("cancelledBookings", cancelledBookings);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading bookings: " + e.getMessage());
        }
        
        return "admin-bookings";
    }
    
    @PostMapping("/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id, 
                                    @RequestParam String status, 
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<Booking> bookingOptional = bookingService.getBookingById(id);
            if (bookingOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/admin/bookings";
            }
            
            bookingService.updateBookingStatus(id, status);
            
            redirectAttributes.addFlashAttribute("success", 
                "Booking status updated to " + status + " successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating booking status: " + e.getMessage());
        }
        
        return "redirect:/admin/bookings";
    }
    
    @PostMapping("/bookings/{id}/assign-driver")
    public String assignDriverToBooking(@PathVariable Long id, 
                                      @RequestParam Long driverId,
                                      RedirectAttributes redirectAttributes) {
        try {
            Optional<Booking> bookingOptional = bookingService.getBookingById(id);
            if (bookingOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/admin/bookings";
            }
            
            Optional<User> driverOptional = userService.findUserById(driverId);
            if (driverOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Driver not found");
                return "redirect:/admin/bookings";
            }
            
            User driver = driverOptional.get();
            
            if (!"DRIVER".equals(driver.getRole()) || !Boolean.TRUE.equals(driver.getApproved())) {
                redirectAttributes.addFlashAttribute("error", 
                    "Selected user is not an approved driver");
                return "redirect:/admin/bookings";
            }
            
            // Update booking with driver
            Booking booking = bookingOptional.get();
            booking.setDriver(driver);
            bookingService.updateBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", 
                "Driver assigned to booking successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error assigning driver: " + e.getMessage());
        }
        
        return "redirect:/admin/bookings";
    }
    
    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/products")
    public String manageProducts(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        // For now, we'll use a placeholder - you can implement Vehicle entity later
        model.addAttribute("products", List.of()); // Empty list for now
        return "admin-products";
    }

    @GetMapping("/products/add")
    public String showAddProductForm(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        // Placeholder for product form
        return "admin-add-product";
    }
    
    // ========== REPORTS GENERATION ==========

    @GetMapping("/reports")
    public String generateReports(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            List<User> allUsers = userService.getAllUsers();
            List<Booking> allBookings = bookingService.getAllBookings();
            
            // Calculate report statistics
            long totalUsers = allUsers.size();
            long totalDrivers = allUsers.stream()
                .filter(user -> "DRIVER".equals(user.getRole()))
                .count();
            long totalBookings = allBookings.size();
            
            // Revenue calculation (placeholder)
            double totalRevenue = allBookings.stream()
                .mapToDouble(booking -> booking.getPrice() != null ? booking.getPrice() : 0.0)
                .sum();
            
            // Booking status breakdown
            Map<String, Long> bookingStatusCount = allBookings.stream()
                .collect(Collectors.groupingBy(
                    Booking::getStatus,
                    Collectors.counting()
                ));
            
            // User registration by month (placeholder)
            Map<String, Long> userRegistrations = Map.of(
                "January", 15L,
                "February", 23L,
                "March", 18L
            );
            
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalDrivers", totalDrivers);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("bookingStatusCount", bookingStatusCount);
            model.addAttribute("userRegistrations", userRegistrations);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error generating reports: " + e.getMessage());
        }
        
        return "admin-reports";
    }

    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadReport(HttpSession session,
                                               @RequestParam String reportType) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Generate CSV report (placeholder implementation)
            String csvContent = generateCSVReport(reportType);
            byte[] csvBytes = csvContent.getBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "tripnest-report-" + reportType + ".csv");
            
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String generateCSVReport(String reportType) {
        // Simple CSV generation - expand based on your needs
        StringBuilder csv = new StringBuilder();
        
        switch (reportType) {
            case "users":
                csv.append("ID,Username,Email,Role,Approved\n");
                List<User> users = userService.getAllUsers();
                for (User user : users) {
                    csv.append(user.getId()).append(",")
                       .append(user.getUsername()).append(",")
                       .append(user.getEmail()).append(",")
                       .append(user.getRole()).append(",")
                       .append(user.getApproved()).append("\n");
                }
                break;
                
            case "bookings":
                csv.append("ID,Customer,Driver,Status,Price,Date\n");
                List<Booking> bookings = bookingService.getAllBookings();
                for (Booking booking : bookings) {
                    csv.append(booking.getId()).append(",")
                       .append(booking.getCustomerName()).append(",")
                       .append(booking.getDriver() != null ? booking.getDriver().getFirstName() : "N/A").append(",")
                       .append(booking.getStatus()).append(",")
                       .append(booking.getPrice()).append(",")
                       .append(booking.getBookingDate()).append("\n");
                }
                break;
                
            default:
                csv.append("Report Type,Count\n")
                   .append("Total Users,").append(userService.getAllUsers().size()).append("\n")
                   .append("Total Bookings,").append(bookingService.getAllBookings().size()).append("\n");
        }
        
        return csv.toString();
    }
    
    // ========== STATISTICS ==========
    
    @GetMapping("/statistics")
    public String viewStatistics(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            return "redirect:/auth/login";
        }
        
        try {
            List<User> allUsers = userService.getAllUsers();
            
            // Filter different user types
            List<User> drivers = allUsers.stream()
                .filter(user -> "DRIVER".equals(user.getRole()))
                .toList();
                
            List<User> approvedDrivers = drivers.stream()
                .filter(user -> Boolean.TRUE.equals(user.getApproved()))
                .toList();
                
            List<User> pendingDrivers = drivers.stream()
                .filter(user -> !Boolean.TRUE.equals(user.getApproved()))
                .toList();
                
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Booking> pendingBookings = bookingService.getPendingBookings();
            
            // Filter completed bookings
            List<Booking> completedBookings = allBookings.stream()
                .filter(booking -> "COMPLETED".equals(booking.getStatus()))
                .toList();
            
            // Calculate statistics
            long totalUsers = allUsers.size();
            long totalDrivers = drivers.size();
            long totalApprovedDrivers = approvedDrivers.size();
            long totalPendingDrivers = pendingDrivers.size();
            long totalBookings = allBookings.size();
            long totalPendingBookings = pendingBookings.size();
            long totalCompletedBookings = completedBookings.size();
            
            double completionRate = totalBookings > 0 ? 
                (double) totalCompletedBookings / totalBookings * 100 : 0;
            double approvalRate = totalDrivers > 0 ? 
                (double) totalApprovedDrivers / totalDrivers * 100 : 0;
            
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalDrivers", totalDrivers);
            model.addAttribute("totalApprovedDrivers", totalApprovedDrivers);
            model.addAttribute("totalPendingDrivers", totalPendingDrivers);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("totalPendingBookings", totalPendingBookings);
            model.addAttribute("totalCompletedBookings", totalCompletedBookings);
            model.addAttribute("completionRate", Math.round(completionRate * 100.0) / 100.0);
            model.addAttribute("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading statistics: " + e.getMessage());
        }
        
        return "admin-statistics";
    }
    
    // ========== HELPER METHODS ==========
    
    // Add missing service methods to UserService
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    public Optional<User> findUserById(Long id) {
        return userService.findUserById(id);
    }
    
    public User updateUser(User user) {
        return userService.updateUser(user);
    }
}