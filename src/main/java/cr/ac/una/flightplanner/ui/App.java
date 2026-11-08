package cr.ac.una.flightplanner.ui;

import cr.ac.una.flightplanner.algorithm.Criterion;
import cr.ac.una.flightplanner.algorithm.DepthFirstSearch;
import cr.ac.una.flightplanner.algorithm.PathResult;
import cr.ac.una.flightplanner.algorithm.ReachabilityResult;
import cr.ac.una.flightplanner.algorithm.RouteFinder;
import cr.ac.una.flightplanner.io.DataLoadException;
import cr.ac.una.flightplanner.io.DataLoader;
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Ventana principal: mapa a la izquierda, panel de control a la derecha. */
public class App extends Application {

    private final TextField originField = new TextField();
    private final TextField destinationField = new TextField();
    private final ComboBox<Criterion> criterionBox = new ComboBox<>();
    private final TextArea output = new TextArea();
    private final ScrollPane mapScroll = new ScrollPane();
    private final Label flightStatus = new Label();

    private Stage stage;
    private Graph graph;
    private MapPane mapPane;
    private FlightAnimator animator;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        mapScroll.setPannable(true);
        mapScroll.setContent(new Label("Sin datos cargados"));

        BorderPane root = new BorderPane(mapScroll);
        root.setRight(buildPanel());

        stage.setTitle("Planificador de vuelos - EIF207");
        stage.setScene(new Scene(root, 1340, 860));
        stage.show();

        // carpeta con aeropuertos.txt y rutas.txt: primer argumento, o "data" por defecto
        List<String> args = getParameters().getRaw();
        loadData(Path.of(args.isEmpty() ? "data" : args.get(0)));
    }

    private VBox buildPanel() {
        originField.setPromptText("Ej: SJO (o clic en el mapa)");
        destinationField.setPromptText("Ej: MIA (o clic en el mapa)");
        criterionBox.getItems().addAll(Criterion.values());
        criterionBox.getSelectionModel().selectFirst();
        criterionBox.setMaxWidth(Double.MAX_VALUE);

        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle("-fx-font-family: monospace;");
        output.setMinHeight(120);
        VBox.setVgrow(output, Priority.ALWAYS);

        flightStatus.setWrapText(true);
        flightStatus.setMinHeight(70);
        flightStatus.setStyle("-fx-font-family: monospace; -fx-background-color: #FFF3E0; -fx-padding: 6;");
        flightStatus.setMaxWidth(Double.MAX_VALUE);

        Button pause = button("Pausar / continuar", () -> withAnimator(FlightAnimator::togglePause));
        Button restart = button("Reiniciar vuelo", () -> withAnimator(FlightAnimator::restart));
        HBox.setHgrow(pause, Priority.ALWAYS);
        HBox.setHgrow(restart, Priority.ALWAYS);
        HBox animationControls = new HBox(8, pause, restart);

        VBox panel = new VBox(8,
                new Label("Origen"), originField,
                new Label("Destino"), destinationField,
                new Label("Criterio"), criterionBox,
                button("Buscar ruta", this::search),
                animationControls,
                flightStatus,
                button("Consultar aeropuerto (origen)", this::showAirport),
                button("Alcanzables desde el origen", this::showReachableFrom),
                button("Desde donde se llega al destino", this::showReachingTo),
                button("Cancelar ruta origen -> destino", this::cancelRoute),
                button("Restaurar rutas canceladas", this::restoreRoutes),
                button("Limpiar mapa", this::clearMap),
                button("Cargar otros archivos...", this::chooseDataFolder),
                output);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(330);
        return panel;
    }

    private Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void withAnimator(Consumer<FlightAnimator> action) {
        if (animator == null) {
            flightStatus.setText("Primero hay que cargar los archivos de datos.");
            return;
        }
        action.accept(animator);
    }

    /** Detiene el avion; se llama antes de cualquier accion que cambie lo que muestra el mapa. */
    private void stopAnimation() {
        if (animator != null) {
            animator.stop();
        }
        flightStatus.setText("");
    }

    private void loadData(Path folder) {
        try {
            Graph loaded = DataLoader.load(folder.resolve("aeropuertos.txt"), folder.resolve("rutas.txt"));
            stopAnimation();
            graph = loaded;
            mapPane = new MapPane(graph, this::onAirportSelected);
            animator = new FlightAnimator(mapPane, flightStatus::setText);
            mapScroll.setContent(mapPane);
            originField.clear();
            destinationField.clear();
            output.setText("Datos cargados desde " + folder + "\n"
                    + graph.getAirports().size() + " aeropuertos, " + graph.getAllRoutes().size() + " rutas.");
        } catch (DataLoadException e) {
            output.setText(e.getMessage());
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void chooseDataFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Carpeta con aeropuertos.txt y rutas.txt");
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            loadData(selected.toPath());
        }
    }

    // Clic en un aeropuerto: muestra sus datos y lo usa como origen, y luego como destino
    private void onAirportSelected(Airport airport) {
        boolean startOver = originField.getText().isBlank() || !destinationField.getText().isBlank();
        if (startOver) {
            originField.setText(airport.code());
            destinationField.clear();
        } else {
            destinationField.setText(airport.code());
        }
        stopAnimation();
        mapPane.resetView();
        markSelection();
        output.setText(ResultFormatter.airport(graph, airport));
    }

    private void markSelection() {
        mapPane.markAirport(originField.getText(), AirportNode.State.ORIGIN);
        mapPane.markAirport(destinationField.getText(), AirportNode.State.DESTINATION);
    }

    private boolean hasData() {
        if (graph == null) {
            output.setText("Primero hay que cargar los archivos de datos.");
            return false;
        }
        return true;
    }

    // 4.1: datos y rutas salientes de un aeropuerto escrito por texto
    private void showAirport() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        try {
            Airport airport = graph.requireAirport(originField.getText());
            mapPane.resetView();
            mapPane.markAirport(airport.code(), AirportNode.State.ORIGIN);
            output.setText(ResultFormatter.airport(graph, airport));
        } catch (IllegalArgumentException e) {
            output.setText(e.getMessage());
        }
    }

    private void search() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        try {
            Criterion criterion = criterionBox.getValue();
            String from = Graph.normalizeCode(originField.getText());
            String to = Graph.normalizeCode(destinationField.getText());
            Optional<PathResult> result = RouteFinder.find(graph, from, to, criterion);
            if (result.isPresent()) {
                mapPane.showRoute(result.get());
                output.setText(ResultFormatter.route(criterion, result.get()));
                animator.start(result.get());
            } else {
                mapPane.resetView();
                markSelection();
                output.setText("No existe ruta de " + from + " a " + to);
            }
        } catch (IllegalArgumentException e) {
            output.setText(e.getMessage());
        }
    }

    private void showReachableFrom() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        try {
            ReachabilityResult result = DepthFirstSearch.reachableFrom(graph, originField.getText());
            mapPane.showReachability(result, AirportNode.State.ORIGIN);
            output.setText(ResultFormatter.reachableFrom(result));
        } catch (IllegalArgumentException e) {
            output.setText(e.getMessage());
        }
    }

    private void showReachingTo() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        try {
            ReachabilityResult result = DepthFirstSearch.reachingTo(graph, destinationField.getText());
            mapPane.showReachability(result, AirportNode.State.DESTINATION);
            output.setText(ResultFormatter.reachingTo(result));
        } catch (IllegalArgumentException e) {
            output.setText(e.getMessage());
        }
    }

    private void cancelRoute() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        try {
            String from = graph.requireAirport(originField.getText()).code();
            String to = graph.requireAirport(destinationField.getText()).code();
            int removed = graph.cancelRoute(from, to);
            mapPane.resetView();
            markSelection();
            if (removed == 0) {
                output.setText("No hay una ruta activa de " + from + " a " + to + " para cancelar.");
            } else {
                output.setText("Ruta cancelada: " + from + " -> " + to
                        + "\nRutas activas: " + graph.getActiveRouteCount() + " de " + graph.getAllRoutes().size());
            }
        } catch (IllegalArgumentException e) {
            output.setText(e.getMessage());
        }
    }

    private void restoreRoutes() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        int count = graph.getCancelledRoutes().size();
        graph.restoreAll();
        mapPane.resetView();
        markSelection();
        output.setText("Rutas restauradas: " + count);
    }

    private void clearMap() {
        if (!hasData()) {
            return;
        }
        stopAnimation();
        originField.clear();
        destinationField.clear();
        mapPane.resetView();
        output.clear();
    }
}
