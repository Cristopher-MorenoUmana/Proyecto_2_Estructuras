package cr.ac.una.flightplanner.algorithm;

import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public final class BreadthFirstSearch {

    private BreadthFirstSearch() {
    }

    public static Optional<PathResult> fewestFlights(Graph graph, String originCode, String destinationCode) {
        String origin = graph.requireAirport(originCode).code();
        String destination = graph.requireAirport(destinationCode).code();

        Map<String, Route> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(destination)) {
                break;
            }
            for (Route route : graph.getOutgoingRoutes(current)) {
                
                if (visited.add(route.destination())) {
                    previous.put(route.destination(), route);
                    queue.add(route.destination());
                }
            }
        }

        if (!visited.contains(destination)) {
            return Optional.empty();
        }
        return Optional.of(PathResult.fromPredecessors(origin, destination, previous));
    }
}
