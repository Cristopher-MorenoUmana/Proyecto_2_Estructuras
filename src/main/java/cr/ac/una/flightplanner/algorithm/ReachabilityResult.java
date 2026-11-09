package cr.ac.una.flightplanner.algorithm;

import java.util.Set;

public record ReachabilityResult(String reference, Set<String> reached, Set<String> notReached) {
}
