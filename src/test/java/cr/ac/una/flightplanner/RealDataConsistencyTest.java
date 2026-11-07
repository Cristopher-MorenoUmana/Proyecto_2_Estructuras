package cr.ac.una.flightplanner;

import cr.ac.una.flightplanner.algorithm.BreadthFirstSearch;
import cr.ac.una.flightplanner.algorithm.Dijkstra;
import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.io.DataLoader;
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compara BFS y Dijkstra contra Floyd-Warshall (calculado solo aqui, en la prueba) para los 40 x 40 pares
 * de aeropuertos de los archivos entregados, con los tres criterios.
 */
class RealDataConsistencyTest {

    private static final int INF = Integer.MAX_VALUE / 4;

    @Test
    void searchesMatchFloydWarshallForEveryPair() throws Exception {
        Graph graph = DataLoader.load(Path.of("data/aeropuertos.txt"), Path.of("data/rutas.txt"));
        assertEquals(40, graph.getAirports().size());
        assertEquals(202, graph.getAllRoutes().size());

        List<String> codes = graph.getAirports().stream().map(Airport::code).toList();
        int[][] hops = floydWarshall(graph, codes, route -> 1);
        int[][] minutes = floydWarshall(graph, codes, Route::minutes);
        int[][] cost = floydWarshall(graph, codes, Route::cost);

        for (int i = 0; i < codes.size(); i++) {
            for (int j = 0; j < codes.size(); j++) {
                String from = codes.get(i);
                String to = codes.get(j);

                Optional<PathResult> bfs = BreadthFirstSearch.fewestFlights(graph, from, to);
                Optional<PathResult> fastest = Dijkstra.shortestPath(graph, from, to, Route::minutes);
                Optional<PathResult> cheapest = Dijkstra.shortestPath(graph, from, to, Route::cost);

                if (hops[i][j] >= INF) {
                    assertTrue(bfs.isEmpty() && fastest.isEmpty() && cheapest.isEmpty(), from + " -> " + to);
                    continue;
                }
                assertEquals(hops[i][j], bfs.orElseThrow().flights(), "BFS " + from + " -> " + to);
                assertEquals(minutes[i][j], fastest.orElseThrow().totalMinutes(), "minutos " + from + " -> " + to);
                assertEquals(cost[i][j], cheapest.orElseThrow().totalCost(), "costo " + from + " -> " + to);

                assertValidPath(graph, bfs.get(), from, to);
                assertValidPath(graph, fastest.get(), from, to);
                assertValidPath(graph, cheapest.get(), from, to);
            }
        }
    }

    private static void assertValidPath(Graph graph, PathResult path, String from, String to) {
        assertEquals(from, path.codes().get(0));
        assertEquals(to, path.codes().get(path.codes().size() - 1));
        assertEquals(path.codes().size() - 1, path.routes().size());
        for (int k = 0; k < path.routes().size(); k++) {
            Route route = path.routes().get(k);
            assertEquals(path.codes().get(k), route.origin());
            assertEquals(path.codes().get(k + 1), route.destination());
            assertTrue(graph.getOutgoingRoutes(route.origin()).contains(route), "la ruta no existe en el grafo");
        }
    }

    private static int[][] floydWarshall(Graph graph, List<String> codes, ToIntFunction<Route> weight) {
        int n = codes.size();
        int[][] dist = new int[n][n];
        for (int[] row : dist) {
            Arrays.fill(row, INF);
        }
        for (int i = 0; i < n; i++) {
            dist[i][i] = 0;
            for (Route route : graph.getOutgoingRoutes(codes.get(i))) {
                int j = codes.indexOf(route.destination());
                dist[i][j] = Math.min(dist[i][j], weight.applyAsInt(route));
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }
        return dist;
    }
}
