package acs.allocation;

import acs.model.Emergency;
import acs.model.Vehicle;
import acs.routing.RouteFinder;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocatorTest {

    @Test
    void allocatorCanBeCreated() {
        ResourceAllocator allocator = new ResourceAllocator();
        assertNotNull(allocator);
    }

    @Test
    void priorityIncludesWaitingTimeBonus() {
        int priority = PriorityCalculator.calculatePriority(70, 5);
        assertEquals(80, priority);
    }

    @Test
    void routeAwareAllocationChoosesLowestEta() {
        Emergency emergency = new Emergency(
                "E1", "FIRE", "CRITICAL", 5,
                LocalDateTime.now(), "LOC_03"
        );

        Vehicle vehicle1 = new Vehicle(
                "V1", "FIRE_ENGINE", "LOC_01", true
        );
        Vehicle vehicle2 = new Vehicle(
                "V2", "FIRE_ENGINE", "LOC_02", true
        );

        List<RouteFinder.Road> roads = List.of(
                new RouteFinder.Road("R1", "LOC_01", "LOC_03", 10, true),
                new RouteFinder.Road("R2", "LOC_02", "LOC_03", 4, true)
        );

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocateUsingRoutes(
                List.of(emergency), List.of(vehicle1, vehicle2),
                roads, 1.0, new RouteFinder()
        );

        assertEquals(1, result.assignments().size());
        assertEquals("V2", result.assignments().get(0).vehicleId());
        assertEquals(
                4.0,
                result.assignments().get(0).estimatedArrivalMinutes(),
                0.001
        );
        assertFalse(vehicle2.isAvailable());
        assertTrue(vehicle1.isAvailable());
    }

    @Test
    void emergencyBecomesPendingWhenNoRouteExists() {
        Emergency emergency = new Emergency(
                "E1", "FIRE", "CRITICAL", 5,
                LocalDateTime.now(), "LOC_99"
        );

        Vehicle vehicle = new Vehicle(
                "V1", "FIRE_ENGINE", "LOC_01", true
        );

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocateUsingRoutes(
                List.of(emergency), List.of(vehicle),
                List.of(), 1.0, new RouteFinder()
        );

        assertTrue(result.assignments().isEmpty());
        assertEquals(1, result.pending().size());
        assertEquals("E1", result.pending().get(0).emergencyId());
        assertEquals("PENDING", result.pending().get(0).status());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void oneVehicleCannotBeAssignedToTwoEmergencies() {
        Emergency emergency1 = new Emergency(
                "E1", "FIRE", "CRITICAL", 5,
                LocalDateTime.now().minusMinutes(5), "LOC_02"
        );

        Emergency emergency2 = new Emergency(
                "E2", "FIRE", "HIGH", 2,
                LocalDateTime.now(), "LOC_02"
        );

        Vehicle vehicle = new Vehicle(
                "V1", "FIRE_ENGINE", "LOC_01", true
        );

        List<RouteFinder.Road> roads = List.of(
                new RouteFinder.Road("R1", "LOC_01", "LOC_02", 5, true)
        );

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocateUsingRoutes(
                List.of(emergency1, emergency2), List.of(vehicle),
                roads, 1.0, new RouteFinder()
        );

        assertEquals(1, result.assignments().size());
        assertEquals("E1", result.assignments().get(0).emergencyId());
        assertEquals("V1", result.assignments().get(0).vehicleId());

        assertEquals(1, result.pending().size());
        assertEquals("E2", result.pending().get(0).emergencyId());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    void assignedEmergencyHasAssignedStatus() {
        Emergency emergency = new Emergency(
                "E1", "FIRE", "CRITICAL", 5,
                LocalDateTime.now()
        );

        Vehicle vehicle = new Vehicle("V1", "FIRE_ENGINE", true);

        AssignmentResult result = new ResourceAllocator().allocate(
                List.of(emergency), List.of(vehicle),
                (e, v) -> 5.0
        );

        assertEquals(1, result.assignments().size());
        assertEquals("ASSIGNED", result.assignments().get(0).status());
    }

    @Test
    void pendingEmergencyHasPendingStatus() {
        Emergency emergency = new Emergency(
                "E2", "FIRE", "HIGH", 2,
                LocalDateTime.now()
        );

        AssignmentResult result = new ResourceAllocator().allocate(
                List.of(emergency), List.of(),
                (e, v) -> 5.0
        );

        assertEquals(1, result.pending().size());
        assertEquals("PENDING", result.pending().get(0).status());
    }

    @Test
    void higherPriorityEmergencyIsAssignedFirst() {
        Emergency lowPriority = new Emergency(
                "E1", "FIRE", "LOW", 0,
                LocalDateTime.now().minusMinutes(10)
        );

        Emergency highPriority = new Emergency(
                "E2", "FIRE", "CRITICAL", 0,
                LocalDateTime.now()
        );

        Vehicle vehicle1 = new Vehicle("V1", "FIRE_ENGINE", true);
        Vehicle vehicle2 = new Vehicle("V2", "FIRE_ENGINE", true);

        AssignmentResult result = new ResourceAllocator().allocate(
                List.of(lowPriority, highPriority),
                List.of(vehicle1, vehicle2),
                (emergency, vehicle) -> 5.0
        );

        assertEquals(2, result.assignments().size());
        assertEquals("E2", result.assignments().get(0).emergencyId());
        assertEquals("E1", result.assignments().get(1).emergencyId());
        assertTrue(result.pending().isEmpty());
    }

    @Test
    void olderEmergencyIsAssignedFirstWhenPrioritiesAreEqual() {
        LocalDateTime olderTime =
                LocalDateTime.of(2026, 10, 28, 10, 0);

        LocalDateTime newerTime =
                LocalDateTime.of(2026, 10, 29, 10, 0);

        Emergency newerEmergency = new Emergency(
                "E1", "FIRE", "HIGH", 0, newerTime
        );

        Emergency olderEmergency = new Emergency(
                "E2", "FIRE", "HIGH", 0, olderTime
        );

        Vehicle vehicle1 = new Vehicle("V1", "FIRE_ENGINE", true);
        Vehicle vehicle2 = new Vehicle("V2", "FIRE_ENGINE", true);

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocate(
                List.of(newerEmergency, olderEmergency),
                List.of(vehicle1, vehicle2),
                (emergency, vehicle) -> 5.0
        );

        assertEquals(2, result.assignments().size());
        assertEquals("E2", result.assignments().get(0).emergencyId());
        assertEquals("E1", result.assignments().get(1).emergencyId());
        assertTrue(result.pending().isEmpty());
    }

    @Test
    void emergencyIdBreaksTieWhenPriorityAndTimeAreEqual() {
        LocalDateTime sameTime =
                LocalDateTime.of(2026, 10, 29, 10, 0);

        Emergency emergencyB = new Emergency(
                "E2", "FIRE", "HIGH", 0, sameTime
        );

        Emergency emergencyA = new Emergency(
                "E1", "FIRE", "HIGH", 0, sameTime
        );

        Vehicle vehicle1 = new Vehicle("V1", "FIRE_ENGINE", true);
        Vehicle vehicle2 = new Vehicle("V2", "FIRE_ENGINE", true);

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocate(
                List.of(emergencyB, emergencyA),
                List.of(vehicle1, vehicle2),
                (emergency, vehicle) -> 5.0
        );

        assertEquals(2, result.assignments().size());

        // E1 must come before E2 when priority and creation time are equal.
        assertEquals("E1", result.assignments().get(0).emergencyId());
        assertEquals("E2", result.assignments().get(1).emergencyId());
    }

    @Test
    void assignsCorrectVehicleTypeForEachEmergency() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 29, 10, 0);

        Emergency fire = new Emergency(
                "E1", "FIRE", "CRITICAL", 0, time
        );

        Emergency accident = new Emergency(
                "E2", "ACCIDENT", "HIGH", 0, time
        );

        Emergency flood = new Emergency(
                "E3", "FLOOD", "MEDIUM", 0, time
        );

        Vehicle fireEngine = new Vehicle("V1", "FIRE_ENGINE", true);
        Vehicle ambulance = new Vehicle("V2", "AMBULANCE", true);
        Vehicle rescueVehicle = new Vehicle("V3", "RESCUE_VEHICLE", true);

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocate(
                List.of(fire, accident, flood),
                List.of(fireEngine, ambulance, rescueVehicle),
                (emergency, vehicle) -> 5.0
        );

        assertEquals(3, result.assignments().size());
        assertTrue(result.pending().isEmpty());

        assertEquals("V1", findVehicle(result, "E1"));
        assertEquals("V2", findVehicle(result, "E2"));
        assertEquals("V3", findVehicle(result, "E3"));
    }

    private String findVehicle(AssignmentResult result, String emergencyId) {
        return result.assignments().stream()
                .filter(a -> a.emergencyId().equals(emergencyId))
                .map(AssignmentResult.Assignment::vehicleId)
                .findFirst()
                .orElse(null);
    }

    @Test
    void unsupportedEmergencyTypeBecomesPending() {
        Emergency emergency = new Emergency(
                "E4", "EARTHQUAKE", "HIGH", 0,
                LocalDateTime.of(2026, 10, 29, 10, 0)
        );

        Vehicle vehicle = new Vehicle("V1", "FIRE_ENGINE", true);

        ResourceAllocator allocator = new ResourceAllocator();

        AssignmentResult result = allocator.allocate(
                List.of(emergency),
                List.of(vehicle),
                (e, v) -> 5.0
        );

        assertTrue(result.assignments().isEmpty());
        assertEquals(1, result.pending().size());
        assertEquals("E4", result.pending().get(0).emergencyId());
        assertEquals("PENDING", result.pending().get(0).status());
        assertEquals(
                "Unsupported emergency type.",
                result.pending().get(0).reason()
        );

        assertTrue(vehicle.isAvailable());
    }
}