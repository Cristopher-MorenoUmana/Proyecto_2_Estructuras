package cr.ac.una.flightplanner.algorithm;

import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.util.Optional;

public final class RouteFinder {

    private RouteFinder() {
    }

    public static Optional<PathResult> find(Graph graph, String origin, String destination, Criterion criterion) {
        return switch (criterion) {
            case FEWEST_FLIGHTS -> BreadthFirstSearch.fewestFlights(graph, origin, destination);
            case FASTEST -> Dijkstra.shortestPath(graph, origin, destination, Route::minutes);
            case CHEAPEST -> Dijkstra.shortestPath(graph, origin, destination, Route::cost);
        };
    }
}
