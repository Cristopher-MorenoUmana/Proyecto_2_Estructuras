package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.algorithm.ReachabilityResult;
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mapa completo. Es un Pane (no un StackPane) porque todo se ubica con coordenadas absolutas;
 * el orden de las capas decide que queda encima de que.
 */
public class MapPane extends Pane {

    private static final double MARGIN = 40;

    private final Graph graph;
    private final Map<String, AirportNode> nodes = new LinkedHashMap<>();
    // por identidad: dos lineas iguales en el archivo siguen siendo dos rutas distintas
    private final Map<Route, RouteEdge> edges = new IdentityHashMap<>();

    private final Group edgeLayer = new Group();
    private final Group nodeLayer = new Group();
    private final Group overlayLayer = new Group(); // encima de todo: el avion animado (FlightAnimator)

    public MapPane(Graph graph, Consumer<Airport> onAirportSelected) {
        this.graph = graph;

        double maxX = 0;
        double maxY = 0;
        for (Airport airport : graph.getAirports()) {
            AirportNode node = new AirportNode(airport);
            node.setOnSelected(onAirportSelected);
            nodes.put(airport.code(), node);
            nodeLayer.getChildren().add(node);
            maxX = Math.max(maxX, airport.x());
            maxY = Math.max(maxY, airport.y());
        }

        for (Route route : graph.getAllRoutes()) {
            RouteEdge edge = new RouteEdge(route, nodes.get(route.origin()), nodes.get(route.destination()));
            edges.put(route, edge);
            edgeLayer.getChildren().add(edge);
        }

        getChildren().addAll(edgeLayer, nodeLayer, overlayLayer);
        setPrefSize(Math.max(400, maxX + MARGIN), Math.max(400, maxY + MARGIN));
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setStyle("-fx-background-color: white;");
    }

    public Group getOverlayLayer() {
        return overlayLayer;
    }

    public AirportNode getNode(String code) {
        return nodes.get(Graph.normalizeCode(code));
    }

    /** Todo vuelve a su aspecto normal; las rutas canceladas siguen punteadas. */
    public void resetView() {
        for (AirportNode node : nodes.values()) {
            node.setState(AirportNode.State.NORMAL);
        }
        refreshEdges();
    }

    public void refreshEdges() {
        for (Map.Entry<Route, RouteEdge> entry : edges.entrySet()) {
            boolean cancelled = graph.isCancelled(entry.getKey());
            entry.getValue().setState(cancelled ? RouteEdge.State.CANCELLED : RouteEdge.State.NORMAL);
        }
    }

    public void markAirport(String code, AirportNode.State state) {
        AirportNode node = getNode(code);
        if (node != null) {
            node.setState(state);
        }
    }

    public void showRoute(PathResult result) {
        resetView();
        for (Route route : result.routes()) {
            edges.get(route).setState(RouteEdge.State.HIGHLIGHTED);
        }
        for (String code : result.codes()) {
            markAirport(code, AirportNode.State.ON_ROUTE);
        }
        markAirport(result.codes().get(0), AirportNode.State.ORIGIN);
        if (result.codes().size() > 1) {
            markAirport(result.codes().get(result.codes().size() - 1), AirportNode.State.DESTINATION);
        }
    }

    /** Alcanzables en verde, no alcanzables en gris y el aeropuerto de referencia con su propio color. */
    public void showReachability(ReachabilityResult result, AirportNode.State referenceState) {
        resetView();
        for (String code : result.reached()) {
            markAirport(code, AirportNode.State.REACHABLE);
        }
        for (String code : result.notReached()) {
            markAirport(code, AirportNode.State.UNREACHABLE);
        }
        markAirport(result.reference(), referenceState);
    }
}
