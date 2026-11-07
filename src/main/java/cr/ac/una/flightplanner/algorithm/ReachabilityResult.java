package cr.ac.una.flightplanner.algorithm;

import java.util.Set;

/**
 * Resultado de un recorrido DFS. reference es el aeropuerto de partida y no aparece en ninguno de los dos conjuntos.
 * reached: aeropuertos que el recorrido logro visitar. notReached: los demas.
 */
public record ReachabilityResult(String reference, Set<String> reached, Set<String> notReached) {
}
