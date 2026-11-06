package cr.ac.una.flightplanner.algorithm;

/** Criterio de busqueda que elige el usuario en la interfaz. */
public enum Criterion {
    FEWEST_FLIGHTS("Menos escalas (BFS)"),
    FASTEST("Mas rapida (Dijkstra, minutos)"),
    CHEAPEST("Mas barata (Dijkstra, dolares)");

    private final String label;

    Criterion(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    // el ComboBox muestra toString()
    @Override
    public String toString() {
        return label;
    }
}
