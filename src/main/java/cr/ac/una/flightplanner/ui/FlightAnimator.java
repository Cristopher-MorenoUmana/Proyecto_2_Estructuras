package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.model.Route;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.function.Consumer;

/**
 * Anima un avion que recorre la ruta tramo por tramo.
 *
 * Funciona como una maquina de estados que avanza en cada cuadro (AnimationTimer):
 *   VOLANDO el tramo i  -> la posicion se interpola entre el aeropuerto i y el i + 1
 *   ESCALA en i + 1     -> el avion se queda quieto STOPOVER_MS milisegundos
 *   TERMINADO           -> el avion queda en el destino
 * Pausar detiene el timer; al continuar se descarta el tiempo que estuvo en pausa.
 */
public class FlightAnimator {

    private static final double MS_PER_MINUTE = 8;   // 100 min de vuelo = 0,8 s de animacion
    private static final double MIN_LEG_MS = 900;
    private static final double MAX_LEG_MS = 3500;
    private static final double STOPOVER_MS = 900;

    private final MapPane map;
    private final Consumer<String> status;
    private final Group plane = buildPlane();
    private final AnimationTimer timer;

    private PathResult path;
    private int leg;              // indice del tramo actual (0 .. vuelos - 1)
    private boolean inStopover;   // true mientras espera en la escala al final del tramo leg
    private boolean finished;
    private boolean running;
    private double elapsedMs;     // tiempo transcurrido dentro del estado actual
    private long lastNanos;       // marca del cuadro anterior; 0 = todavia no hay
    private int doneMinutes;      // minutos de los tramos ya completados
    private int doneCost;         // costo de los tramos ya completados

    public FlightAnimator(MapPane map, Consumer<String> status) {
        this.map = map;
        this.status = status;
        plane.setVisible(false);
        plane.setMouseTransparent(true);
        map.getOverlayLayer().getChildren().add(plane);
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                step(now);
            }
        };
    }

    /** Empieza a animar una ruta nueva. Si la ruta no tiene vuelos (origen = destino) no hace nada. */
    public void start(PathResult newPath) {
        stop();
        if (newPath.flights() == 0) {
            return;
        }
        path = newPath;
        restart();
    }

    /** Vuelve a empezar desde el origen la ultima ruta animada. */
    public void restart() {
        if (path == null) {
            status.accept("Primero busque una ruta para animar.");
            return;
        }
        timer.stop();
        leg = 0;
        inStopover = false;
        finished = false;
        elapsedMs = 0;
        doneMinutes = 0;
        doneCost = 0;
        plane.setVisible(true);
        placePlane(0);
        resume();
    }

    public void togglePause() {
        if (path == null) {
            status.accept("Primero busque una ruta para animar.");
            return;
        }
        if (finished) {
            return;
        }
        if (running) {
            running = false;
            timer.stop();
            publish();
        } else {
            resume();
        }
    }

    /** Detiene la animacion y oculta el avion (se usa al limpiar el mapa o cambiar de consulta). */
    public void stop() {
        timer.stop();
        running = false;
        path = null;
        plane.setVisible(false);
    }

    private void resume() {
        running = true;
        lastNanos = 0; // el primer cuadro solo toma la referencia de tiempo
        timer.start();
        publish();
    }

    private void step(long now) {
        if (lastNanos == 0) {
            lastNanos = now;
            return;
        }
        elapsedMs += (now - lastNanos) / 1_000_000.0;
        lastNanos = now;

        if (inStopover) {
            if (elapsedMs >= STOPOVER_MS) {
                inStopover = false;
                elapsedMs = 0;
                leg++;
            }
            placePlane(0);
        } else {
            Route route = path.routes().get(leg);
            double progress = elapsedMs / legDuration(route);
            if (progress >= 1) {
                doneMinutes += route.minutes();
                doneCost += route.cost();
                placePlane(1);
                elapsedMs = 0;
                if (leg == path.flights() - 1) {
                    finished = true;
                    running = false;
                    timer.stop();
                } else {
                    inStopover = true;
                }
            } else {
                placePlane(progress);
            }
        }
        publish();
    }

    // la duracion en pantalla es proporcional a los minutos del vuelo, con un minimo y un maximo
    private static double legDuration(Route route) {
        return Math.max(MIN_LEG_MS, Math.min(MAX_LEG_MS, route.minutes() * MS_PER_MINUTE));
    }

    /** Ubica el avion en el tramo actual: t = 0 en el aeropuerto de salida, t = 1 en el de llegada. */
    private void placePlane(double t) {
        AirportNode from = map.getNode(path.codes().get(leg));
        AirportNode to = map.getNode(path.codes().get(leg + 1));
        double x = from.getCenterX() + (to.getCenterX() - from.getCenterX()) * t;
        double y = from.getCenterY() + (to.getCenterY() - from.getCenterY()) * t;
        plane.setLayoutX(x);
        plane.setLayoutY(y);
        // la figura apunta hacia +x; se gira hacia el destino del tramo
        plane.setRotate(Math.toDegrees(Math.atan2(to.getCenterY() - from.getCenterY(),
                to.getCenterX() - from.getCenterX())));
    }

    private void publish() {
        if (path == null) {
            return;
        }
        Route route = path.routes().get(leg);
        int minutes = doneMinutes;
        int cost = doneCost;
        StringBuilder text = new StringBuilder();

        if (finished) {
            text.append("Vuelo completado en ").append(path.codes().get(path.codes().size() - 1));
        } else if (inStopover) {
            text.append("Escala en ").append(route.destination())
                    .append(" (").append(leg + 1).append(" de ").append(path.flights()).append(" tramos listos)");
        } else {
            text.append("Tramo ").append(leg + 1).append(" de ").append(path.flights()).append(": ")
                    .append(route.origin()).append(" -> ").append(route.destination());
            // durante el tramo, los minutos avanzan con el avion y el costo del tramo ya se cuenta
            minutes += (int) Math.round(route.minutes() * Math.min(1, elapsedMs / legDuration(route)));
            cost += route.cost();
        }
        text.append("\nMinutos acumulados: ").append(minutes);
        text.append("\nCosto acumulado: $").append(cost);
        if (!running && !finished) {
            text.append("\n(En pausa)");
        }
        status.accept(text.toString());
    }

    // avion simple centrado en (0, 0) y apuntando hacia la derecha, para que setRotate gire sobre su centro
    private static Group buildPlane() {
        Polygon body = new Polygon(
                11, 0,
                4, -2,
                -1, -9,
                -4, -9,
                -2, -2,
                -8, -2,
                -11, -6,
                -11, 6,
                -8, 2,
                -2, 2,
                -4, 9,
                -1, 9,
                4, 2);
        body.setFill(Color.web("#EF6C00"));
        body.setStroke(Color.web("#4E342E"));
        body.setStrokeWidth(0.8);
        return new Group(body);
    }
}
