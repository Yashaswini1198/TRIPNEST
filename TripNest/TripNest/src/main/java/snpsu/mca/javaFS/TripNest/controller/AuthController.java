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
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "success", required = false) String success,
                               Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        }
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                           @RequestParam String password, 
                           HttpSession session, 
                           Model model) {
        try {
            Optional<User> userOptional = userService.loginUser(email, password);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60);
                
                // Redirect based on role
                String role = user.getRole();
                if ("DRIVER".equals(role)) {
                    if (!Boolean.TRUE.equals(user.getApproved())) {
                        model.addAttribute("error", "Your driver account is pending approval.");
                        return "login";
                    }
                    return "redirect:/driver/dashboard";
                } else if ("ADMIN".equals(role)) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/customer/dashboard";
                }
            } else {
                model.addAttribute("error", "Invalid email or password. Please try again.");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during login: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                              @RequestParam String confirmPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            // Check if passwords match
            if (!user.getPassword().equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("user", user);
                return "register";
            }
            
            // Generate username from email if not provided
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                String username = user.getEmail().split("@")[0];
                user.setUsername(username);
            }
            
            // Set default role
            user.setRole("CUSTOMER");
            user.setApproved(true);
            
            User registeredUser = userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login?success=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

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
            
            // Generate username from email if not provided
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                String username = user.getEmail().split("@")[0];
                user.setUsername(username);
            }
            
            // Set driver role
            user.setRole("DRIVER");
            user.setApproved(false);
            
            User registeredUser = userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", 
                "Driver registration submitted! Your account is pending admin approval.");
            return "redirect:/auth/login?success=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("user", user);
            return "driver-register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully.");
        return "redirect:/auth/login?logout=true";
    }
}