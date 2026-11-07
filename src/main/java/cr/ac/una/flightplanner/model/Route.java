package cr.ac.una.flightplanner.model;

/** Arista dirigida con dos pesos: duracion en minutos y costo en dolares. */
public record Route(String origin, String destination, int minutes, int cost) {
}
