package snpsu.mca.javaFS.TripNest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import snpsu.mca.javaFS.TripNest.entity.User;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/driver")
public class DriverController {
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"DRIVER".equals(user.getRole())) {
            return "redirect:/auth/login";
        }
        
        // Check if driver is approved
        if (!Boolean.TRUE.equals(user.getApproved())) {
            model.addAttribute("message", "Your account is pending admin approval.");
        }
        
        model.addAttribute("user", user);
        return "driver-dashboard";
    }
}