package acs;

import acs.model.Vehicle;
import acs.routing.RouteFinder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationDataTest {

    @Test
    void createsExpectedNumberOfLocations() {
        List<String> locations = SimulationData.createLocations();

        assertEquals(24, locations.size());
        assertEquals("LOC_01", locations.get(0));
        assertEquals("LOC_24", locations.get(23));
    }

    @Test
    void createsExpectedNumberOfRoads() {
        List<RouteFinder.Road> roads = SimulationData.createRoads();

        assertEquals(38, roads.size());
    }

    @Test
    void createsSixAvailableVehicles() {
        List<Vehicle> vehicles = SimulationData.createVehicles();

        assertEquals(6, vehicles.size());
        assertTrue(vehicles.stream().allMatch(Vehicle::isAvailable));
    }

    @Test
    void everyVehicleStartsAtAKnownLocation() {
        List<String> locations = SimulationData.createLocations();
        List<Vehicle> vehicles = SimulationData.createVehicles();

        for (Vehicle vehicle : vehicles) {
            assertTrue(
                    locations.contains(vehicle.getLocation()),
                    "Unknown starting location for " + vehicle.getId()
            );
        }
    }

    @Test
    void roadNetworkConnectsTheFirstAndLastLocations() {
        RouteFinder finder = new RouteFinder();

        var route = finder.findShortestRoute(
                "LOC_01",
                "LOC_24",
                SimulationData.createRoads(),
                1.0
        );

        assertTrue(route.isPresent());
        assertTrue(route.get().totalMinutes() > 0);
    }
}