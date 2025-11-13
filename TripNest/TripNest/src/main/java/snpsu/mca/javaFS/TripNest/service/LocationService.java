package snpsu.mca.javaFS.TripNest.service;

import org.springframework.stereotype.Service;

@Service
public class LocationService {
    
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two coordinates
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance in kilometers
    }
    
    public Double estimateDistance(String pickupLocation, String dropLocation) {
        // Mock implementation - in real app, use Google Maps API
        return Math.random() * 20 + 2; // Random distance between 2-22 km
    }
}