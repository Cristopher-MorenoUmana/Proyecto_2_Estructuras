package cr.ac.una.flightplanner.model;

<<<<<<< HEAD
/** Vertice del grafo. Las coordenadas x, y vienen del archivo (ventana de 1000 x 1000). */
public record Airport(String code, double x, double y, String city, String country) {

    /** Ciudad y pais sin los guiones bajos que usa el archivo. */
=======
public record Airport(String code, double x, double y, String city, String country) {

>>>>>>> ae3814dbe5fbf1ddd07335e9c9a738a048ce5277
    public String displayName() {
        return city.replace('_', ' ') + ", " + country.replace('_', ' ');
    }
}
