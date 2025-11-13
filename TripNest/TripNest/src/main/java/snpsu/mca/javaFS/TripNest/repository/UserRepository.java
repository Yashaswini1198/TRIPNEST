package snpsu.mca.javaFS.TripNest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import snpsu.mca.javaFS.TripNest.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    List<User> findByRole(String role);
    List<User> findByRoleAndApproved(String role, Boolean approved);
    
    List<User> findByApproved(Boolean approved);
    
    long countByRole(String role);
    long countByRoleAndApproved(String role, Boolean approved);
}