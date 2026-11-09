package cr.ac.una.flightplanner.algorithm;

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

    @Override
    public String toString() {
        return label;
    }
}
