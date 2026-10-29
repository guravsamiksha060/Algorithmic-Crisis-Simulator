package acs.allocation;

import acs.model.Emergency;
import acs.model.Vehicle;
import acs.routing.RouteFinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleBiFunction;

public class ResourceAllocator {

    public ResourceAllocator() {
    }

    public AssignmentResult allocate(
            List<Emergency> emergencies,
            List<Vehicle> vehicles,
            ToDoubleBiFunction<Emergency, Vehicle> etaCalculator) {

        if (emergencies == null || vehicles == null || etaCalculator == null) {
            throw new IllegalArgumentException("Inputs cannot be null.");
        }

        List<Emergency> sorted = new ArrayList<>(emergencies);

        sorted.sort(
                Comparator.comparingInt(this::calculatePriority)
                        .reversed()
                        .thenComparing(Emergency::createdAt)
                        .thenComparing(Emergency::id)
        );

        List<AssignmentResult.Assignment> assignments = new ArrayList<>();
        List<AssignmentResult.PendingEmergency> pending = new ArrayList<>();

        for (Emergency emergency : sorted) {

            int priority = calculatePriority(emergency);
            String requiredType = requiredVehicleType(emergency.type());

            if (requiredType == null) {
                pending.add(new AssignmentResult.PendingEmergency(
                        emergency.id(),
                        "Unsupported emergency type.",
                        priority,
                        "PENDING"
                ));
                continue;
            }

            List<Vehicle> candidates = vehicles.stream()
                    .filter(Vehicle::isAvailable)
                    .filter(vehicle ->
                            vehicle.getType().trim()
                                    .equalsIgnoreCase(requiredType))
                    .toList();

            Vehicle bestVehicle = null;
            double bestEta = Double.POSITIVE_INFINITY;

            for (Vehicle vehicle : candidates) {

                double eta;

                try {
                    eta = etaCalculator.applyAsDouble(emergency, vehicle);
                } catch (RuntimeException exception) {
                    continue;
                }

                if (!Double.isFinite(eta) || eta < 0) {
                    continue;
                }

                if (bestVehicle == null
                        || eta < bestEta
                        || (Double.compare(eta, bestEta) == 0
                        && vehicle.getId().compareTo(bestVehicle.getId()) < 0)) {

                    bestVehicle = vehicle;
                    bestEta = eta;
                }
            }

            if (bestVehicle == null) {

                String reason = candidates.isEmpty()
                        ? "No available suitable vehicle."
                        : "No valid route available.";

                pending.add(new AssignmentResult.PendingEmergency(
                        emergency.id(),
                        reason,
                        priority,
                        "PENDING"
                ));

                continue;
            }

            // Reserve the vehicle immediately.
            bestVehicle.reserve();

            assignments.add(new AssignmentResult.Assignment(
                    emergency.id(),
                    bestVehicle.getId(),
                    bestEta,
                    priority,
                    "ASSIGNED"
            ));
        }

        return new AssignmentResult(assignments, pending);
    }

    public AssignmentResult allocateUsingRoutes(
            List<Emergency> emergencies,
            List<Vehicle> vehicles,
            List<RouteFinder.Road> roads,
            double trafficMultiplier,
            RouteFinder routeFinder) {

        if (emergencies == null || vehicles == null
                || roads == null || routeFinder == null) {
            throw new IllegalArgumentException("Inputs cannot be null.");
        }

        if (!Double.isFinite(trafficMultiplier) || trafficMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "Traffic multiplier must be at least 1.0.");
        }

        return allocate(emergencies, vehicles, (emergency, vehicle) -> {

            String start = vehicle.getLocation();
            String destination = emergency.location();

            if (start == null || start.isBlank()
                    || destination == null || destination.isBlank()) {
                return Double.NaN;
            }

            return routeFinder.findShortestRoute(
                            start,
                            destination,
                            roads,
                            trafficMultiplier)
                    .map(RouteFinder.RouteResult::totalMinutes)
                    .orElse(Double.NaN);
        });
    }

    private int calculatePriority(Emergency emergency) {
        int severityScore = SeverityScorer.scoreOf(emergency.severity());

        return PriorityCalculator.calculatePriority(
                severityScore,
                emergency.waitingMinutes()
        );
    }

    private String requiredVehicleType(String type) {

        if (type == null) {
            return null;
        }

        return switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "FIRE" -> "FIRE_ENGINE";
            case "ACCIDENT" -> "AMBULANCE";
            case "FLOOD" -> "RESCUE_VEHICLE";
            default -> null;
        };
    }
}