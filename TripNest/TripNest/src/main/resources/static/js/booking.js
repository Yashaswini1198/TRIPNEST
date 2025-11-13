// Booking functionality
class BookingManager {
    constructor() {
        this.initializeEventListeners();
    }
    
    initializeEventListeners() {
        // Calculate fare when distance or vehicle type changes
        const distanceInput = document.getElementById('distance');
        const vehicleTypeSelect = document.getElementById('vehicleType');
        
        if (distanceInput && vehicleTypeSelect) {
            distanceInput.addEventListener('input', this.calculateFare.bind(this));
            vehicleTypeSelect.addEventListener('change', this.calculateFare.bind(this));
        }
        
        // Handle booking form submission
        const bookingForm = document.getElementById('bookingForm');
        if (bookingForm) {
            bookingForm.addEventListener('submit', this.handleBookingSubmit.bind(this));
        }
    }
    
    calculateFare() {
        const distance = parseFloat(document.getElementById('distance').value) || 0;
        const vehicleType = document.getElementById('vehicleType').value;
        
        const baseFare = 50;
        const perKmRate = this.getPerKmRate(vehicleType);
        const totalFare = baseFare + (distance * perKmRate);
        
        document.getElementById('fareDisplay').textContent = '₹' + totalFare.toFixed(2);
        document.getElementById('fare').value = totalFare.toFixed(2);
    }
    
    getPerKmRate(vehicleType) {
        const rates = {
            'standard': 8,
            'premium': 12,
            'suv': 15,
            'luxury': 20
        };
        return rates[vehicleType] || 8;
    }
    
    handleBookingSubmit(event) {
        const pickupLocation = document.getElementById('pickupLocation').value;
        const dropLocation = document.getElementById('dropLocation').value;
        
        if (!pickupLocation || !dropLocation) {
            event.preventDefault();
            alert('Please enter both pickup and drop locations');
            return;
        }
        
        // Show loading state
        const submitBtn = event.target.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Booking...';
    }
    
    // OTP verification for drivers
    verifyOTP(bookingId) {
        const otp = prompt('Enter OTP to complete ride:');
        if (otp) {
            fetch(`/driver/complete-ride/${bookingId}?otp=${otp}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            }).then(response => {
                if (response.ok) {
                    location.reload();
                } else {
                    alert('Invalid OTP!');
                }
            });
        }
    }
}

// Initialize when document is ready
document.addEventListener('DOMContentLoaded', function() {
    new BookingManager();
});