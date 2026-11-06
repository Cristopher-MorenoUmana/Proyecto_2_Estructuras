package cr.ac.una.flightplanner.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Graph {

    private final Map<String, Airport> airports = new LinkedHashMap<>();
    private final Map<String, List<Route>> adjacency = new HashMap<>();
    private final List<Route> allRoutes = new ArrayList<>(); // todas las del archivo

    public static String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    public void addAirport(Airport airport) {
        if (airports.containsKey(airport.code())) {
            throw new IllegalArgumentException("Aeropuerto repetido: " + airport.code());
        }
        airports.put(airport.code(), airport);
        adjacency.put(airport.code(), new ArrayList<>());
    }

    public void addRoute(Route route) {
        List<Route> outgoing = adjacency.get(route.origin());
        if (outgoing == null) {
            throw new IllegalArgumentException("El origen " + route.origin() + " no existe");
        }
        if (!airports.containsKey(route.destination())) {
            throw new IllegalArgumentException("El destino " + route.destination() + " no existe");
        }
        outgoing.add(route);
        allRoutes.add(route);
    }

    public boolean hasAirport(String code) {
        return airports.containsKey(normalizeCode(code));
    }

    /** Devuelve null si el codigo no existe. */
    public Airport getAirport(String code) {
        return airports.get(normalizeCode(code));
    }

    /** Igual que getAirport, pero lanza una excepcion con mensaje para el usuario. */
    public Airport requireAirport(String code) {
        String normalized = normalizeCode(code);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Falta el codigo de aeropuerto");
        }
        Airport airport = airports.get(normalized);
        if (airport == null) {
            throw new IllegalArgumentException("El aeropuerto " + normalized + " no existe");
        }
        return airport;
    }

    public Collection<Airport> getAirports() {
        return Collections.unmodifiableCollection(airports.values());
    }

    /** Rutas que salen del aeropuerto (lista vacia si no existe). */
    public List<Route> getOutgoingRoutes(String code) {
        List<Route> outgoing = adjacency.get(normalizeCode(code));
        return outgoing == null ? List.of() : Collections.unmodifiableList(outgoing);
    }

    /** Todas las rutas del archivo. */
    public List<Route> getAllRoutes() {
        return Collections.unmodifiableList(allRoutes);
    }
}
