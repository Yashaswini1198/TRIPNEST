package snpsu.mca.javaFS.TripNest.dto;

public class BookingRequest {
    private String pickupLocation;
    private String dropLocation;
    private Double distance;
    private String vehicleType;
    private Long customerId;

    // Constructors
    public BookingRequest() {}

    public BookingRequest(String pickupLocation, String dropLocation, Double distance, String vehicleType) {
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.distance = distance;
        this.vehicleType = vehicleType;
    }

    // Getters and Setters
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}