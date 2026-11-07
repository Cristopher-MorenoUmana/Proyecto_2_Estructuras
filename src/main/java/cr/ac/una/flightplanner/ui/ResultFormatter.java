package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.algorithm.Criterion;
import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.algorithm.ReachabilityResult;
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.util.List;

/** Convierte resultados en el texto que se muestra en el panel. */
public final class ResultFormatter {

    private ResultFormatter() {
    }

    public static String route(Criterion criterion, PathResult result) {
        if (result.flights() == 0) {
            return "El origen y el destino son el mismo aeropuerto: 0 vuelos.";
        }
        StringBuilder text = new StringBuilder();
        text.append(criterion.label()).append('\n');
        text.append(String.join(" -> ", result.codes())).append("\n\n");
        text.append("Vuelos:  ").append(result.flights()).append('\n');
        text.append("Minutos: ").append(duration(result.totalMinutes())).append('\n');
        text.append("Costo:   $").append(result.totalCost());
        return text.toString();
    }

    public static String airport(Graph graph, Airport airport) {
        List<Route> outgoing = graph.getOutgoingRoutes(airport.code());
        StringBuilder text = new StringBuilder();
        text.append(airport.code()).append(" - ").append(airport.displayName()).append('\n');
        text.append("Posicion: (").append(plain(airport.x())).append(", ").append(plain(airport.y())).append(")\n\n");
        text.append("Rutas salientes (").append(outgoing.size()).append("):\n");
        for (Route route : outgoing) {
            text.append(String.format("  -> %-4s %4d min  $%d%n", route.destination(), route.minutes(), route.cost()));
        }
        return text.toString();
    }

    public static String reachableFrom(ReachabilityResult result) {
        return "Desde " + result.reference() + " SI se llega a (" + result.reached().size() + "):\n"
                + joinOrNone(result.reached())
                + "\n\nDesde " + result.reference() + " NO se llega a (" + result.notReached().size() + "):\n"
                + joinOrNone(result.notReached());
    }

    public static String reachingTo(ReachabilityResult result) {
        return "Hacia " + result.reference() + " SI se llega desde (" + result.reached().size() + "):\n"
                + joinOrNone(result.reached())
                + "\n\nHacia " + result.reference() + " NO se llega desde (" + result.notReached().size() + "):\n"
                + joinOrNone(result.notReached());
    }

    private static String joinOrNone(Iterable<String> codes) {
        String joined = String.join(", ", codes);
        return joined.isEmpty() ? "(ninguno)" : joined;
    }

    private static String duration(int minutes) {
        return minutes + " (" + (minutes / 60) + " h " + (minutes % 60) + " min)";
    }

    private static String plain(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
