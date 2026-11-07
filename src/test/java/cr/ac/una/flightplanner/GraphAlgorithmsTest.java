package cr.ac.una.flightplanner;

import cr.ac.una.flightplanner.algorithm.BreadthFirstSearch;
<<<<<<< HEAD
import cr.ac.una.flightplanner.algorithm.DepthFirstSearch;
import cr.ac.una.flightplanner.algorithm.Dijkstra;
import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.algorithm.ReachabilityResult;
=======
import cr.ac.una.flightplanner.algorithm.Dijkstra;
import cr.ac.una.flightplanner.algorithm.PathResult;
>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
<<<<<<< HEAD
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
=======

import static org.junit.jupiter.api.Assertions.assertEquals;
>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Grafo pequeno donde la ruta con menos vuelos, la mas rapida y la mas barata son tres rutas distintas. */
class GraphAlgorithmsTest {

    private static Graph sampleGraph() {
        Graph graph = new Graph();
        for (String code : List.of("A", "B", "C", "D", "E", "F")) {
            graph.addAirport(new Airport(code, 0, 0, "Ciudad", "Pais"));
        }
        graph.addRoute(new Route("A", "D", 100, 50));  // directa: menos vuelos
        graph.addRoute(new Route("A", "B", 10, 100));  // A-B-D: la mas rapida
        graph.addRoute(new Route("B", "D", 10, 100));
        graph.addRoute(new Route("A", "C", 60, 5));    // A-C-E-D: la mas barata
        graph.addRoute(new Route("C", "E", 60, 5));
        graph.addRoute(new Route("E", "D", 60, 5));
<<<<<<< HEAD
        // F queda aislado
=======
        // F queda aislado (no llega ninguna ruta)
>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
        return graph;
    }

    @Test
    void bfsFindsFewestFlights() {
        PathResult result = BreadthFirstSearch.fewestFlights(sampleGraph(), "A", "D").orElseThrow();
        assertEquals(List.of("A", "D"), result.codes());
        assertEquals(1, result.flights());
        assertEquals(100, result.totalMinutes());
        assertEquals(50, result.totalCost());
    }

    @Test
    void dijkstraFindsFastestAndCheapestWithTheSameMethod() {
        Graph graph = sampleGraph();

        PathResult fastest = Dijkstra.shortestPath(graph, "A", "D", Route::minutes).orElseThrow();
        assertEquals(List.of("A", "B", "D"), fastest.codes());
        assertEquals(20, fastest.totalMinutes());
        assertEquals(200, fastest.totalCost());

        PathResult cheapest = Dijkstra.shortestPath(graph, "A", "D", Route::cost).orElseThrow();
        assertEquals(List.of("A", "C", "E", "D"), cheapest.codes());
        assertEquals(15, cheapest.totalCost());
        assertEquals(180, cheapest.totalMinutes());
    }

    @Test
    void noRouteGivesEmptyResult() {
        Graph graph = sampleGraph();
        assertTrue(BreadthFirstSearch.fewestFlights(graph, "A", "F").isEmpty());
        assertTrue(Dijkstra.shortestPath(graph, "A", "F", Route::minutes).isEmpty());
        // las rutas son dirigidas: de D no se sale
        assertTrue(BreadthFirstSearch.fewestFlights(graph, "D", "A").isEmpty());
    }

    @Test
    void sameOriginAndDestinationHasZeroFlights() {
        Graph graph = sampleGraph();
        PathResult bfs = BreadthFirstSearch.fewestFlights(graph, "A", "A").orElseThrow();
        assertEquals(List.of("A"), bfs.codes());
        assertEquals(0, bfs.flights());
        PathResult dijkstra = Dijkstra.shortestPath(graph, "A", "A", Route::cost).orElseThrow();
        assertEquals(0, dijkstra.totalCost());
    }

    @Test
    void unknownCodeThrowsMessageForUser() {
        Graph graph = sampleGraph();
        assertThrows(IllegalArgumentException.class, () -> BreadthFirstSearch.fewestFlights(graph, "A", "ZZZ"));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortestPath(graph, "", "A", Route::minutes));
<<<<<<< HEAD
        assertThrows(IllegalArgumentException.class, () -> DepthFirstSearch.reachableFrom(graph, "ZZZ"));
=======
>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
    }

    @Test
    void codesAreCaseInsensitive() {
        Optional<PathResult> result = BreadthFirstSearch.fewestFlights(sampleGraph(), " a ", "d");
        assertTrue(result.isPresent());
        assertEquals(List.of("A", "D"), result.get().codes());
    }

    @Test
<<<<<<< HEAD
    void dfsSplitsReachableAndUnreachable() {
        Graph graph = sampleGraph();

        ReachabilityResult from = DepthFirstSearch.reachableFrom(graph, "A");
        assertEquals(Set.of("B", "C", "D", "E"), from.reached());
        assertEquals(Set.of("F"), from.notReached());

        ReachabilityResult to = DepthFirstSearch.reachingTo(graph, "D");
        assertEquals(Set.of("A", "B", "C", "E"), to.reached());
        assertEquals(Set.of("F"), to.notReached());
    }

    @Test
    void cancelledRoutesChangeTheResultAndRestoreBringsThemBack() {
        Graph graph = sampleGraph();

        assertEquals(1, graph.cancelRoute("A", "B"));
        assertTrue(graph.isCancelled(new Route("A", "B", 10, 100)));
        assertEquals(5, graph.getActiveRouteCount());
        PathResult withoutShortcut = Dijkstra.shortestPath(graph, "A", "D", Route::minutes).orElseThrow();
        assertEquals(List.of("A", "D"), withoutShortcut.codes());

        assertEquals(0, graph.cancelRoute("A", "B")); // ya estaba cancelada
        assertEquals(0, graph.cancelRoute("A", "F")); // nunca existio

        graph.restoreAll();
        assertFalse(graph.isCancelled(new Route("A", "B", 10, 100)));
        assertEquals(6, graph.getActiveRouteCount());
        PathResult restored = Dijkstra.shortestPath(graph, "A", "D", Route::minutes).orElseThrow();
        assertEquals(List.of("A", "B", "D"), restored.codes());
    }

    @Test
    void cancellingCanIsolateAnAirport() {
        Graph graph = sampleGraph();
        graph.cancelRoute("A", "D");
        graph.cancelRoute("B", "D");
        graph.cancelRoute("E", "D");
        assertEquals(Set.of("B", "C", "E"), DepthFirstSearch.reachableFrom(graph, "A").reached());
        assertTrue(DepthFirstSearch.reachingTo(graph, "D").reached().isEmpty());
    }

    @Test
=======
>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
    void dijkstraRejectsNegativeWeights() {
        Graph graph = sampleGraph();
        graph.addRoute(new Route("D", "A", 5, -20)); // un "reembolso"
        // se detecta al explorar la ruta negativa (aqui, saliendo de D)
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.shortestPath(graph, "D", "E", Route::cost));
    }
}
