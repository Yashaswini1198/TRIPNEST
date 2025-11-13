package snpsu.mca.javaFS.TripNest.controller;

import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    // ========== LOGIN ENDPOINTS ==========

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "success", required = false) String success,
                               @RequestParam(value = "registered", required = false) String registered,
                               Model model) {
        
        // Add error message if login failed
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        
        // Add success message if logged out
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        
        // Add success message after registration
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        
        if (registered != null) {
            model.addAttribute("success", "Driver registration submitted! Waiting for admin approval.");
        }
        
        // Add empty user object for the form
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                           @RequestParam String password, 
                           HttpSession session, 
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Login attempt for: " + email);
            
            Optional<User> userOptional = userService.loginUser(email, password);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("Login successful for: " + user.getEmail());
                
                // Check if driver is approved
                if ("DRIVER".equals(user.getRole()) && !Boolean.TRUE.equals(user.getApproved())) {
                    model.addAttribute("error", "Your driver account is pending admin approval. Please try again later.");
                    model.addAttribute("email", email);
                    return "login";
                }
                
                // Set user in session
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // Redirect based on role
                String role = user.getRole();
                if ("DRIVER".equals(role)) {
                    return "redirect:/driver/dashboard";
                } else if ("ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/customer/dashboard";
                }
            } else {
                System.out.println("Login failed for: " + email);
                model.addAttribute("error", "Invalid email or password. Please try again.");
                model.addAttribute("email", email);
                return "login";
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            model.addAttribute("error", "Login failed: " + e.getMessage());
            model.addAttribute("email", email);
            return "login";
        }
    }

    // ========== REGISTRATION ENDPOINTS ==========

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                              @RequestParam String confirmPassword,
                              @RequestParam(defaultValue = "USER") String role,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            // Check if passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                return "register";
            }
            
            // Set role as String
            user.setRole(role);
            
            User registeredUser = userService.registerUser(user);
            
            if ("DRIVER".equals(role)) {
                redirectAttributes.addFlashAttribute("success", 
                    "Driver registration submitted! Your account is pending admin approval.");
                return "redirect:/user/login?registered=true";
            } else {
                redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
                return "redirect:/user/login?success=true";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    // ========== DRIVER REGISTRATION ==========

    @GetMapping("/register/driver")
    public String showDriverRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "driver-register";
    }

    @PostMapping("/register/driver")
    public String registerDriver(@ModelAttribute User user,
                                @RequestParam String confirmPassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            // Check if passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                return "driver-register";
            }
            
            // Set role as String
            user.setRole("DRIVER");
            User registeredDriver = userService.registerUser(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Driver registration submitted! Waiting for admin approval.");
            return "redirect:/user/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "driver-register";
        }
    }

    // ========== LOGOUT ==========

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully.");
        return "redirect:/user/login?logout=true";
    }

    // ========== PROFILE ENDPOINTS ==========

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }

    // ========== TEST ENDPOINTS ==========

    @GetMapping("/test/create-user")
    @ResponseBody
    public String createTestUser() {
        try {
            // Delete if exists
            userService.findByEmail("user@tripptest.com").ifPresent(user -> {
                // You might need to add delete method to repository
            });
            
            // Create fresh user
            User user = new User();
            user.setUsername("tripptestuser");
            user.setEmail("user@tripptest.com");
            user.setPassword("password123"); // Will be encoded by service
            user.setFirstName("TrippTest");
            user.setLastName("User");
            user.setRole("USER");
            user.setApproved(true);
            
            User savedUser = userService.registerUser(user);
            return "✅ Test user created: user@tripptest.com / password123 (ID: " + savedUser.getId() + ")";
            
        } catch (Exception e) {
            return "❌ Error creating user: " + e.getMessage();
        }
    }

    // ========== ERROR HANDLING ==========

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "error";
    }
}