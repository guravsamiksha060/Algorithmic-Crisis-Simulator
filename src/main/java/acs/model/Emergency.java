package acs.model;

import java.time.LocalDateTime;

public record Emergency(
        String id,
        String type,
        String severity,
        double waitingMinutes,
        LocalDateTime createdAt,
        String location
) {
    // Keeps compatibility with existing five-argument calls
    public Emergency(
            String id,
            String type,
            String severity,
            double waitingMinutes,
            LocalDateTime createdAt
    ) {
        this(id, type, severity, waitingMinutes, createdAt, null);
    }

    public Emergency {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Emergency ID cannot be empty.");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Emergency type cannot be empty.");
        }

        if (severity == null || severity.isBlank()) {
            throw new IllegalArgumentException(
                    "Severity cannot be empty.");
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Creation time cannot be null.");
        }

        if (!Double.isFinite(waitingMinutes)
                || waitingMinutes < 0) {
            throw new IllegalArgumentException(
                    "Waiting time must be non-negative.");
        }

        if (location != null && location.isBlank()) {
            throw new IllegalArgumentException(
                    "Location cannot be blank.");
        }
    }
}
