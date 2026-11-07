package cr.ac.una.flightplanner.io;

/** Error al leer los archivos de datos; el mensaje ya esta pensado para mostrarse al usuario. */
public class DataLoadException extends Exception {

    private static final long serialVersionUID = 1L;

    public DataLoadException(String message) {
        super(message);
    }
}
