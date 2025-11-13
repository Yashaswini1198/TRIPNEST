package snpsu.mca.javaFS.TripNest.repository;

import snpsu.mca.javaFS.TripNest.entity.Driver;
import snpsu.mca.javaFS.TripNest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUser(User user);
    List<Driver> findByIsAvailableTrue();
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    Optional<Driver> findByVehicleNumber(String vehicleNumber);
}