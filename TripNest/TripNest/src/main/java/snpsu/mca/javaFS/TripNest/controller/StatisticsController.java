package snpsu.mca.javaFS.TripNest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class StatisticsController {

    // Change the URL to avoid conflict
    @GetMapping("/admin/analytics")  // ← Changed from /admin/statistics
    public String showStatistics(Model model) {
        // Sample data - replace with actual data from your service
        model.addAttribute("totalUsers", 15);
        model.addAttribute("totalDrivers", 4);
        model.addAttribute("totalBookings", 10);
        model.addAttribute("totalRevenue", 367.75);
        
        // Status distribution - use LinkedHashMap to maintain order
        Map<String, Integer> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("pending", 2);
        statusDistribution.put("confirmed", 2);
        statusDistribution.put("completed", 5);
        statusDistribution.put("cancelled", 1);
        
        model.addAttribute("statusDistribution", statusDistribution);
        
        return "admin-statistics";
    }
}