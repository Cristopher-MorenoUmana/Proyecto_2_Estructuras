package cr.ac.una.flightplanner;

import cr.ac.una.flightplanner.ui.App;
import javafx.application.Application;

/**
 * Punto de entrada. Es una clase aparte (que no extiende Application) para que el programa tambien
 * arranque desde un jar o desde el IDE sin el error "faltan los componentes de JavaFX".
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
