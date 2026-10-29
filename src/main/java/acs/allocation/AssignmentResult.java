package acs.allocation;

import java.util.List;

public record AssignmentResult(
        List<Assignment> assignments,
        List<PendingEmergency> pending) {

    public AssignmentResult {
        if (assignments == null || pending == null) {
            throw new IllegalArgumentException(
                    "Assignments and pending lists cannot be null.");
        }

        assignments = List.copyOf(assignments);
        pending = List.copyOf(pending);
    }

    public record Assignment(
            String emergencyId,
            String vehicleId,
            double estimatedArrivalMinutes,
            int priority,
            String status) {

        public Assignment {
            if (emergencyId == null || emergencyId.isBlank()
                    || vehicleId == null || vehicleId.isBlank()) {
                throw new IllegalArgumentException(
                        "Emergency ID and vehicle ID are required.");
            }

            if (!Double.isFinite(estimatedArrivalMinutes)
                    || estimatedArrivalMinutes < 0) {
                throw new IllegalArgumentException(
                        "ETA must be finite and non-negative.");
            }

            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException(
                        "Assignment status cannot be empty.");
            }
        }

        // Keeps existing four-argument calls compatible.
        public Assignment(
                String emergencyId,
                String vehicleId,
                double estimatedArrivalMinutes,
                int priority) {
            this(
                    emergencyId,
                    vehicleId,
                    estimatedArrivalMinutes,
                    priority,
                    "ASSIGNED");
        }
    }

    public record PendingEmergency(
            String emergencyId,
            String reason,
            int priority,
            String status) {

        public PendingEmergency {
            if (emergencyId == null || emergencyId.isBlank()) {
                throw new IllegalArgumentException(
                        "Emergency ID cannot be empty.");
            }

            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "Pending reason cannot be empty.");
            }

            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException(
                        "Pending status cannot be empty.");
            }
        }

        // Keeps existing three-argument calls compatible.
        public PendingEmergency(
                String emergencyId,
                String reason,
                int priority) {
            this(
                    emergencyId,
                    reason,
                    priority,
                    "PENDING");
        }
    }
}