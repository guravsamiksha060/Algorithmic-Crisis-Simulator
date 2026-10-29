package acs.model;

public class Vehicle {

    private final String id;
    private final String type;
    private final String location;
    private boolean available;

    // Keep the existing constructor for compatibility
    public Vehicle(String id, String type, boolean available) {
        this(id, type, null, available);
    }

    // New constructor with vehicle location
    public Vehicle(String id, String type, String location,
                   boolean available) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be empty.");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle type cannot be empty.");
        }

        if (location != null && location.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle location cannot be blank.");
        }

        this.id = id;
        this.type = type;
        this.location = location;
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return available;
    }

    public void reserve() {
        if (!available) {
            throw new IllegalStateException(
                    "Vehicle is already reserved.");
        }

        available = false;
    }
}