package acs;

import acs.model.Vehicle;
import acs.routing.RouteFinder;

import java.util.ArrayList;
import java.util.List;

public final class SimulationData {

    private SimulationData() {
    }

    // 24 locations: LOC_01 to LOC_24
    public static List<String> createLocations() {
        List<String> locations = new ArrayList<>();

        for (int i = 1; i <= 24; i++) {
            locations.add(String.format("LOC_%02d", i));
        }

        return List.copyOf(locations);
    }

    // 38 illustrative roads for initial testing.
    // Replace travel times and connections when official data is available.
    public static List<RouteFinder.Road> createRoads() {
        List<RouteFinder.Road> roads = new ArrayList<>();

        // 23 connecting roads linking all 24 locations.
        double[] times = {
                5, 7, 4, 6, 8, 3, 5, 7, 4, 6, 5, 8,
                3, 6, 4, 7, 5, 3, 8, 6, 4, 5, 7
        };

        for (int i = 1; i <= 23; i++) {
            roads.add(new RouteFinder.Road(
                    String.format("RD_%02d", i),
                    String.format("LOC_%02d", i),
                    String.format("LOC_%02d", i + 1),
                    times[i - 1],
                    true
            ));
        }

        // 15 additional connecting roads.
        int[][] connections = {
                {1, 4}, {2, 6}, {3, 8}, {4, 9}, {5, 11},
                {6, 12}, {7, 14}, {8, 16}, {9, 17}, {10, 19},
                {11, 20}, {12, 22}, {13, 24}, {15, 21}, {18, 24}
        };

        double[] extraTimes = {
                9, 8, 10, 7, 11, 6, 9, 8, 5, 10,
                7, 9, 6, 8, 7
        };

        for (int i = 0; i < connections.length; i++) {
            roads.add(new RouteFinder.Road(
                    String.format("RD_%02d", 24 + i),
                    String.format("LOC_%02d", connections[i][0]),
                    String.format("LOC_%02d", connections[i][1]),
                    extraTimes[i],
                    true
            ));
        }

        return List.copyOf(roads);
    }

    // Six illustrative vehicles: two of each type.
    public static List<Vehicle> createVehicles() {
        return new ArrayList<>(List.of(
                new Vehicle("VEH_01", "FIRE_ENGINE", "LOC_01", true),
                new Vehicle("VEH_02", "FIRE_ENGINE", "LOC_12", true),

                new Vehicle("VEH_03", "AMBULANCE", "LOC_05", true),
                new Vehicle("VEH_04", "AMBULANCE", "LOC_18", true),

                new Vehicle("VEH_05", "RESCUE_VEHICLE", "LOC_09", true),
                new Vehicle("VEH_06", "RESCUE_VEHICLE", "LOC_23", true)
        ));
    }
}