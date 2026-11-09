package cr.ac.una.flightplanner.algorithm;

import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class DepthFirstSearch {

    private DepthFirstSearch() {
    }

    public static ReachabilityResult reachableFrom(Graph graph, String originCode) {
        String origin = graph.requireAirport(originCode).code();
        Set<String> visited = traverse(origin, code -> {
            List<String> next = new ArrayList<>();
            for (Route route : graph.getOutgoingRoutes(code)) {
                next.add(route.destination());
            }
            return next;
        });
        return split(graph, origin, visited);
    }

    public static ReachabilityResult reachingTo(Graph graph, String destinationCode) {
        String destination = graph.requireAirport(destinationCode).code();
        Map<String, List<String>> reversed = new HashMap<>();
        for (Airport airport : graph.getAirports()) {
            for (Route route : graph.getOutgoingRoutes(airport.code())) {
                reversed.computeIfAbsent(route.destination(), k -> new ArrayList<>()).add(route.origin());
            }
        }
        Set<String> visited = traverse(destination, code -> reversed.getOrDefault(code, List.of()));
        return split(graph, destination, visited);
    }

    private static Set<String> traverse(String start, Function<String, List<String>> neighbors) {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);
        while (!stack.isEmpty()) {
            String current = stack.pop();

            if (!visited.add(current)) {
                continue;
            }
            for (String next : neighbors.apply(current)) {
                if (!visited.contains(next)) {
                    stack.push(next);
                }
            }
        }
        return visited;
    }

    private static ReachabilityResult split(Graph graph, String reference, Set<String> visited) {
        Set<String> reached = new LinkedHashSet<>(visited);
        reached.remove(reference);
        Set<String> notReached = new LinkedHashSet<>();
        for (Airport airport : graph.getAirports()) {
            if (!visited.contains(airport.code())) {
                notReached.add(airport.code());
            }
        }
        return new ReachabilityResult(reference, reached, notReached);
    }
}
