package cr.ac.una.flightplanner.model;


public record Airport(String code, double x, double y, String city, String country) {

    
    public String displayName() {
        return city.replace('_', ' ') + ", " + country.replace('_', ' ');
    }
}
