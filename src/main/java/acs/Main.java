package acs;

import acs.allocation.AssignmentResult;
import acs.allocation.ResourceAllocator;
import acs.model.Emergency;
import acs.model.Vehicle;
import acs.routing.RouteFinder;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("    ALGORITHMIC CRISIS SIMULATOR");
        System.out.println("======================================");

        // Load demo simulation data
        List<String> locations = SimulationData.createLocations();
        List<RouteFinder.Road> roads = SimulationData.createRoads();
        List<Vehicle> vehicles = SimulationData.createVehicles();

        System.out.println("Locations: " + locations.size());
        System.out.println("Roads: " + roads.size());
        System.out.println("Vehicles: " + vehicles.size());

        // Create sample emergencies
        List<Emergency> emergencies = List.of(
                new Emergency(
                        "EMG_01", "FIRE", "CRITICAL", 10,
                        LocalDateTime.now().minusMinutes(10), "LOC_08"
                ),
                new Emergency(
                        "EMG_02", "ACCIDENT", "HIGH", 5,
                        LocalDateTime.now().minusMinutes(5), "LOC_15"
                ),
                new Emergency(
                        "EMG_03", "FLOOD", "MEDIUM", 2,
                        LocalDateTime.now().minusMinutes(2), "LOC_22"
                )
        );

        // Perform resource allocation
        ResourceAllocator allocator = new ResourceAllocator();
        RouteFinder routeFinder = new RouteFinder();

        AssignmentResult result = allocator.allocateUsingRoutes(
                emergencies,
                vehicles,
                roads,
                1.0,
                routeFinder
        );

        // Display assigned emergencies
        System.out.println("\n========== ASSIGNED EMERGENCIES ==========");

        if (result.assignments().isEmpty()) {
            System.out.println("No emergencies were assigned.");
        }

        for (AssignmentResult.Assignment assignment : result.assignments()) {
            System.out.println("--------------------------------------");
            System.out.println("Emergency ID : " + assignment.emergencyId());
            System.out.println("Vehicle ID   : " + assignment.vehicleId());
            System.out.printf("ETA          : %.2f minutes%n",
                    assignment.estimatedArrivalMinutes());
            System.out.println("Priority     : " + assignment.priority());
            System.out.println("Status       : " + assignment.status());
        }

        // Display pending emergencies
        System.out.println("\n========== PENDING EMERGENCIES ==========");

        if (result.pending().isEmpty()) {
            System.out.println("No pending emergencies.");
        }

        for (AssignmentResult.PendingEmergency pending : result.pending()) {
            System.out.println("--------------------------------------");
            System.out.println("Emergency ID : " + pending.emergencyId());
            System.out.println("Priority     : " + pending.priority());
            System.out.println("Status       : " + pending.status());
            System.out.println("Reason       : " + pending.reason());
        }

        System.out.println("\n======================================");
        System.out.println("Total emergencies : " + emergencies.size());
        System.out.println("Assigned          : " + result.assignments().size());
        System.out.println("Pending           : " + result.pending().size());
        System.out.println("Simulation completed.");
        System.out.println("======================================");
    }
}