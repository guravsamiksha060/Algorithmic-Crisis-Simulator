package acs.routing;

import java.util.*;

public class RouteFinder {

    public record Road(
            String id,
            String from,
            String to,
            double travelMinutes,
            boolean bidirectional
    ) {
        public Road {
            if (id == null || id.isBlank()
                    || from == null || from.isBlank()
                    || to == null || to.isBlank()) {
                throw new IllegalArgumentException(
                        "Road ID and locations cannot be empty."
                );
            }

            if (!Double.isFinite(travelMinutes)
                    || travelMinutes < 0) {
                throw new IllegalArgumentException(
                        "Travel time must be finite and non-negative."
                );
            }
        }
    }

    public record RouteResult(
            List<String> locations,
            double totalMinutes
    ) {
        public RouteResult {
            locations = List.copyOf(locations);
        }
    }

    private record Edge(String destination, double minutes) {
    }

    private record QueueEntry(String location, double minutes) {
    }

    public Optional<RouteResult> findShortestRoute(
            String start,
            String destination,
            List<Road> roads,
            double trafficMultiplier
    ) {
        if (start == null || start.isBlank()
                || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException(
                    "Start and destination cannot be empty."
            );
        }

        if (roads == null) {
            throw new IllegalArgumentException(
                    "Road list cannot be null."
            );
        }

        if (!Double.isFinite(trafficMultiplier)
                || trafficMultiplier < 1.0) {
            throw new IllegalArgumentException(
                    "Traffic multiplier must be at least 1.0."
            );
        }

        Map<String, List<Edge>> graph = new HashMap<>();

        for (Road road : roads) {
            double time = road.travelMinutes() * trafficMultiplier;

            if (!Double.isFinite(time)) {
                throw new IllegalArgumentException(
                        "Calculated travel time is too large."
                );
            }

            graph.computeIfAbsent(
                    road.from(), key -> new ArrayList<>()
            ).add(new Edge(road.to(), time));

            graph.computeIfAbsent(
                    road.to(), key -> new ArrayList<>()
            );

            if (road.bidirectional()) {
                graph.get(road.to()).add(
                        new Edge(road.from(), time)
                );
            }
        }

        if (!graph.containsKey(start)
                || !graph.containsKey(destination)) {
            return Optional.empty();
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        PriorityQueue<QueueEntry> queue =
                new PriorityQueue<>(
                        Comparator.comparingDouble(
                                QueueEntry::minutes
                        ).thenComparing(QueueEntry::location)
                );

        distances.put(start, 0.0);
        queue.add(new QueueEntry(start, 0.0));

        while (!queue.isEmpty()) {
            QueueEntry current = queue.poll();

            if (Double.compare(
                    current.minutes(),
                    distances.getOrDefault(
                            current.location(),
                            Double.POSITIVE_INFINITY
                    )
            ) != 0) {
                continue;
            }

            if (current.location().equals(destination)) {
                break;
            }

            for (Edge edge : graph.get(current.location())) {
                double newTime =
                        current.minutes() + edge.minutes();

                if (!Double.isFinite(newTime)) {
                    continue;
                }

                double oldTime = distances.getOrDefault(
                        edge.destination(),
                        Double.POSITIVE_INFINITY
                );

                if (newTime < oldTime) {
                    distances.put(edge.destination(), newTime);
                    previous.put(
                            edge.destination(),
                            current.location()
                    );

                    queue.add(
                            new QueueEntry(
                                    edge.destination(), newTime
                            )
                    );
                }
            }
        }

        if (!distances.containsKey(destination)) {
            return Optional.empty();
        }

        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);

            if (current.equals(start)) {
                break;
            }

            current = previous.get(current);
        }

        if (!path.get(path.size() - 1).equals(start)) {
            return Optional.empty();
        }

        Collections.reverse(path);

        return Optional.of(
                new RouteResult(
                        path,
                        distances.get(destination)
                )
        );
    }
}