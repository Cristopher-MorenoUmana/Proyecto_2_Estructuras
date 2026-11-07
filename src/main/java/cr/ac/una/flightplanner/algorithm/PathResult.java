package cr.ac.una.flightplanner.algorithm;

import cr.ac.una.flightplanner.model.Route;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Resultado de una busqueda: la secuencia de codigos y las rutas usadas en cada tramo.
 * Si origen y destino son el mismo aeropuerto, codes tiene un solo elemento y routes esta vacia.
 */
public record PathResult(List<String> codes, List<Route> routes) {

    public int flights() {
        return routes.size();
    }

    public int totalMinutes() {
        int sum = 0;
        for (Route route : routes) {
            sum += route.minutes();
        }
        return sum;
    }

    public int totalCost() {
        int sum = 0;
        for (Route route : routes) {
            sum += route.cost();
        }
        return sum;
    }

    /**
     * Reconstruye el camino caminando hacia atras desde el destino.
     * previous guarda, para cada aeropuerto, la ruta con la que se llego a el.
     */
    static PathResult fromPredecessors(String origin, String destination, Map<String, Route> previous) {
        LinkedList<Route> routes = new LinkedList<>();
        String current = destination;
        while (!current.equals(origin)) {
            Route route = previous.get(current);
            routes.addFirst(route);
            current = route.origin();
        }
        List<String> codes = new ArrayList<>();
        codes.add(origin);
        for (Route route : routes) {
            codes.add(route.destination());
        }
        return new PathResult(List.copyOf(codes), List.copyOf(routes));
    }
}
