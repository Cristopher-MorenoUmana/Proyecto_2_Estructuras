package cr.ac.una.flightplanner.algorithm;

import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.ToIntFunction;

public final class Dijkstra {

    private Dijkstra() {
    }

    private record QueueEntry(String code, int distance) {
    }

    public static Optional<PathResult> shortestPath(Graph graph, String originCode, String destinationCode,
                                                    ToIntFunction<Route> weight) {
        String origin = graph.requireAirport(originCode).code();
        String destination = graph.requireAirport(destinationCode).code();

        Map<String, Integer> distance = new HashMap<>();
        Map<String, Route> previous = new HashMap<>();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(Comparator.comparingInt(QueueEntry::distance));
        distance.put(origin, 0);
        queue.add(new QueueEntry(origin, 0));

        while (!queue.isEmpty()) {
            QueueEntry current = queue.poll();
            
            if (current.distance() > distance.get(current.code())) {
                continue;
            }
            
            if (current.code().equals(destination)) {
                break;
            }
            for (Route route : graph.getOutgoingRoutes(current.code())) {
                int w = weight.applyAsInt(route);
                if (w < 0) {
                    throw new IllegalArgumentException("Dijkstra no admite pesos negativos (ruta "
                            + route.origin() + " -> " + route.destination() + ")");
                }
                int candidate = current.distance() + w;
                Integer known = distance.get(route.destination());
                if (known == null || candidate < known) {
                    distance.put(route.destination(), candidate);
                    previous.put(route.destination(), route);
                    queue.add(new QueueEntry(route.destination(), candidate));
                }
            }
        }

        if (!distance.containsKey(destination)) {
            return Optional.empty();
        }
        return Optional.of(PathResult.fromPredecessors(origin, destination, previous));
    }
}
