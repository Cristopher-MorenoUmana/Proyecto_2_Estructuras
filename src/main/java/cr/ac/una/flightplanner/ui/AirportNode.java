package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.model.Airport;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class AirportNode extends Group {

    public static final double RADIUS = 5;

    public enum State {
        NORMAL("#C62828"),
        ORIGIN("#F9A825"),
        DESTINATION("#6A1B9A"),
        ON_ROUTE("#1565C0"),
        REACHABLE("#43A047"),
        UNREACHABLE("#9E9E9E");

        private final Color color;

        State(String hex) {
            this.color = Color.web(hex);
        }

        Color color() {
            return color;
        }
    }

    private final Airport airport;
    private final Circle circle = new Circle(RADIUS);
    private final Text label;

    public AirportNode(Airport airport) {
        this.airport = airport;

        circle.setCenterX(airport.x());
        circle.setCenterY(airport.y());
        circle.setStroke(Color.WHITE);
        circle.setCursor(Cursor.HAND);
        Tooltip.install(circle, new Tooltip(airport.code() + " - " + airport.displayName()));

        label = new Text(airport.code());
        label.setFont(Font.font("System", FontWeight.BOLD, 10));
        label.setX(airport.x() - label.getLayoutBounds().getWidth() / 2);
        label.setY(airport.y() - RADIUS - 3);
        
        label.setMouseTransparent(true);

        getChildren().addAll(circle, label);
        setState(State.NORMAL);
    }

    public Airport getAirport() {
        return airport;
    }

    public double getCenterX() {
        return airport.x();
    }

    public double getCenterY() {
        return airport.y();
    }

    public void setState(State state) {
        circle.setFill(state.color());
        label.setFill(state == State.UNREACHABLE ? Color.GRAY : Color.BLACK);
        if (state != State.NORMAL && state != State.UNREACHABLE) {
            toFront();
        }
    }

    public void setOnSelected(Consumer<Airport> handler) {
        circle.setOnMouseClicked(event -> handler.accept(airport));
    }
}
