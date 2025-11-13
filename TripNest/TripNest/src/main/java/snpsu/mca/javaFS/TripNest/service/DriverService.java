package snpsu.mca.javaFS.TripNest.service;

import snpsu.mca.javaFS.TripNest.entity.Driver;
import snpsu.mca.javaFS.TripNest.entity.User;
import snpsu.mca.javaFS.TripNest.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private UserService userService;
    
    public Driver registerDriver(Driver driver) {
        return driverRepository.save(driver);
    }
    
    public Optional<Driver> getDriverByUser(User user) {
        return driverRepository.findByUser(user);
    }
    
    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByIsAvailableTrue();
    }
    
    public Driver updateDriverLocation(Long driverId, Double latitude, Double longitude) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setCurrentLatitude(latitude);
            driver.setCurrentLongitude(longitude);
            return driverRepository.save(driver);
        }
        throw new RuntimeException("Driver not found");
    }
    
    public Driver updateDriverAvailability(Long driverId, Boolean isAvailable) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setIsAvailable(isAvailable);
            return driverRepository.save(driver);
        }
        throw new RuntimeException("Driver not found");
    }
}