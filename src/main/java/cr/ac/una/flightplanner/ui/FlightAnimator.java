package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.model.Route;
import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.function.Consumer;

public class FlightAnimator {

    private static final double MS_PER_MINUTE = 8;
    private static final double MIN_LEG_MS = 900;
    private static final double MAX_LEG_MS = 3500;
    private static final double STOPOVER_MS = 900;

    private final MapPane map;
    private final Consumer<String> status;
    private final Group plane = buildPlane();
    private final AnimationTimer timer;

    private PathResult path;
    private int leg;
    private boolean inStopover;
    private boolean finished;
    private boolean running;
    private double elapsedMs;
    private long lastNanos;
    private int doneMinutes;
    private int doneCost;

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

   public void start(PathResult newPath) {
        stop();
        if (newPath.flights() == 0) {
            return;
        }
        path = newPath;
        restart();
    }

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

    public void stop() {
        timer.stop();
        running = false;
        path = null;
        plane.setVisible(false);
    }

    private void resume() {
        running = true;
        lastNanos = 0;
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

    private static double legDuration(Route route) {
        return Math.max(MIN_LEG_MS, Math.min(MAX_LEG_MS, route.minutes() * MS_PER_MINUTE));
    }

    private void placePlane(double t) {
        AirportNode from = map.getNode(path.codes().get(leg));
        AirportNode to = map.getNode(path.codes().get(leg + 1));
        double x = from.getCenterX() + (to.getCenterX() - from.getCenterX()) * t;
        double y = from.getCenterY() + (to.getCenterY() - from.getCenterY()) * t;
        plane.setLayoutX(x);
        plane.setLayoutY(y);
        
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
