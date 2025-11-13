package snpsu.mca.javaFS.TripNest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create test user if it doesn't exist
        if (userRepository.findByEmail("user@tripptest.com").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("tripptestuser");
            testUser.setEmail("user@tripptest.com");
            testUser.setPassword(passwordEncoder.encode("password123"));
            testUser.setFirstName("TrippTest");
            testUser.setLastName("User");
            testUser.setRole("USER");
            testUser.setApproved(true);
            
            userRepository.save(testUser);
            System.out.println("✅ Test user created: user@tripptest.com / password123");
        }

        // Create admin user if it doesn't exist
        if (userRepository.findByEmail("admin@tripptest.com").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@tripptest.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setRole("ADMIN");
            adminUser.setApproved(true);
            
            userRepository.save(adminUser);
            System.out.println("✅ Admin user created: admin@tripptest.com / admin123");
        }

        // Create driver user if it doesn't exist
        if (userRepository.findByEmail("driver@tripptest.com").isEmpty()) {
            User driverUser = new User();
            driverUser.setUsername("testdriver");
            driverUser.setEmail("driver@tripptest.com");
            driverUser.setPassword(passwordEncoder.encode("driver123"));
            driverUser.setFirstName("Test");
            driverUser.setLastName("Driver");
            driverUser.setRole("DRIVER");
            driverUser.setApproved(true);
            
            userRepository.save(driverUser);
            System.out.println("✅ Driver user created: driver@tripptest.com / driver123");
        }

        System.out.println("✅ Data initialization completed!");
    }
}