package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.model.Route;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class RouteEdge extends Group {

    public enum State {
        NORMAL, HIGHLIGHTED, CANCELLED
    }

    private static final double SIDE_OFFSET = 3;
    private static final double ARROW_LENGTH = 9;
    private static final double ARROW_HALF_WIDTH = 3.5;

    private final Route route;
    private final Line line = new Line();
    private final Line hitArea = new Line();
    private final Polygon arrow = new Polygon();

    public RouteEdge(Route route, AirportNode from, AirportNode to) {
        this.route = route;

        double dx = to.getCenterX() - from.getCenterX();
        double dy = to.getCenterY() - from.getCenterY();
        double length = Math.hypot(dx, dy);

        if (length > 0) {
            double ux = dx / length;
            double uy = dy / length;
            double nx = -uy;
            double ny = ux;

            double along = Math.sqrt(Math.max(0, AirportNode.RADIUS * AirportNode.RADIUS - SIDE_OFFSET * SIDE_OFFSET));
            double startX = from.getCenterX() + ux * along + nx * SIDE_OFFSET;
            double startY = from.getCenterY() + uy * along + ny * SIDE_OFFSET;
            double endX = to.getCenterX() - ux * along + nx * SIDE_OFFSET;
            double endY = to.getCenterY() - uy * along + ny * SIDE_OFFSET;

            setLinePoints(line, startX, startY, endX, endY);
            setLinePoints(hitArea, startX, startY, endX, endY);

            double baseX = endX - ux * ARROW_LENGTH;
            double baseY = endY - uy * ARROW_LENGTH;
            arrow.getPoints().setAll(
                    endX, endY,
                    baseX + nx * ARROW_HALF_WIDTH, baseY + ny * ARROW_HALF_WIDTH,
                    baseX - nx * ARROW_HALF_WIDTH, baseY - ny * ARROW_HALF_WIDTH);
        }

        hitArea.setStroke(Color.TRANSPARENT);
        hitArea.setStrokeWidth(8);
        Tooltip.install(hitArea, new Tooltip(route.origin() + " -> " + route.destination()
                + "\n" + route.minutes() + " min  |  $" + route.cost()));

        getChildren().addAll(hitArea, line, arrow);
        setState(State.NORMAL);
    }

    public Route getRoute() {
        return route;
    }

    public void setState(State state) {
        Color color;
        double width;
        switch (state) {
            case HIGHLIGHTED -> {
                color = Color.web("#1565C0");
                width = 3;
            }
            case CANCELLED -> {
                color = Color.web("#D32F2F");
                width = 1.5;
            }
            default -> {
                color = Color.web("#B0B0B0");
                width = 1;
            }
        }
        line.setStroke(color);
        line.setStrokeWidth(width);
        arrow.setFill(color);

        if (state == State.CANCELLED) {
            line.getStrokeDashArray().setAll(6.0, 5.0);
        } else {
            line.getStrokeDashArray().clear();
        }
        if (state == State.HIGHLIGHTED) {
            toFront();
        }
    }

    private static void setLinePoints(Line target, double x1, double y1, double x2, double y2) {
        target.setStartX(x1);
        target.setStartY(y1);
        target.setEndX(x2);
        target.setEndY(y2);
    }
}
