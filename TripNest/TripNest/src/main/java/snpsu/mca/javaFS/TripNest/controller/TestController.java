package snpsu.mca.javaFS.TripNest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> testLogin(@RequestParam String email, @RequestParam String password) {
        Optional<User> user = userService.loginUser(email, password);
        if (user.isPresent()) {
            return ResponseEntity.ok("✅ Login successful for: " + user.get().getEmail() + 
                                   " | Role: " + user.get().getRole() + 
                                   " | Approved: " + user.get().getApproved());
        } else {
            return ResponseEntity.badRequest().body("❌ Login failed - Invalid credentials");
        }
    }
    
    @GetMapping("/check-user/{email}")
    public ResponseEntity<?> checkUser(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()) {
            User u = user.get();
            return ResponseEntity.ok("✅ User exists: " + u.getEmail() + 
                                   " | Role: " + u.getRole() + 
                                   " | Approved: " + u.getApproved() +
                                   " | Password length: " + u.getPassword().length());
        } else {
            return ResponseEntity.ok("❌ User not found: " + email);
        }
    }
    
    @GetMapping("/create-test-users")
    public ResponseEntity<?> createTestUsers() {
        try {
            // Test user
            if (userService.findByEmail("user@tripptest.com").isEmpty()) {
                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("user@tripptest.com");
                testUser.setPassword("password123");
                testUser.setFirstName("Test");
                testUser.setLastName("User");
                testUser.setRole("USER");
                testUser.setApproved(true);
                
                userService.registerUser(testUser);
            }
            
            return ResponseEntity.ok("✅ Test users created/verified");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
}