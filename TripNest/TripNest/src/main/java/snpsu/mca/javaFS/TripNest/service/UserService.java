package snpsu.mca.javaFS.TripNest.service;

import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Hash the password before saving
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // Auto-approve regular users, require approval for drivers
        if ("USER".equals(user.getRole())) {
            user.setApproved(true);
        } else {
            user.setApproved(false);
        }
        
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check if user is approved and password matches
            if (Boolean.TRUE.equals(user.getApproved()) && 
                passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // New methods for AdminController
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Helper methods for filtering
    public List<User> getPendingDrivers() {
        return userRepository.findByRoleAndApproved("DRIVER", false);
    }

    public List<User> getApprovedDrivers() {
        return userRepository.findByRoleAndApproved("DRIVER", true);
    }

    public List<User> getAllDrivers() {
        return userRepository.findByRole("DRIVER");
    }
}