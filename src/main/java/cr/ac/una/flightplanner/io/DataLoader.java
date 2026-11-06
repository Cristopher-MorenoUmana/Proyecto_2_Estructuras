package cr.ac.una.flightplanner.io;

import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import cr.ac.una.flightplanner.model.Route;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class DataLoader {

    private DataLoader() {
    }

    public static Graph load(Path airportsFile, Path routesFile) throws DataLoadException {
        Graph graph = new Graph();
        readAirports(airportsFile, graph);
        readRoutes(routesFile, graph);
        return graph;
    }

    private static void readAirports(Path file, Graph graph) throws DataLoadException {
        String name = fileName(file);
        try (Scanner in = new Scanner(file, StandardCharsets.UTF_8)) {
            int count = toInt(token(in), name, "la cantidad de aeropuertos");
            for (int i = 1; i <= count; i++) {
                String code = Graph.normalizeCode(token(in));
                double x = toDouble(token(in), name, "la coordenada x del aeropuerto " + i);
                double y = toDouble(token(in), name, "la coordenada y del aeropuerto " + i);
                String city = token(in);
                String country = token(in);
                try {
                    graph.addAirport(new Airport(code, x, y, city, country));
                } catch (IllegalArgumentException e) {
                    throw new DataLoadException("Archivo " + name + ", aeropuerto " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataLoadException("No se pudo abrir el archivo " + name + ": " + e.getMessage());
        } catch (NoSuchElementException e) {
            throw new DataLoadException("El archivo " + name + " termina antes de lo esperado (faltan datos)");
        }
    }

    private static void readRoutes(Path file, Graph graph) throws DataLoadException {
        String name = fileName(file);
        try (Scanner in = new Scanner(file, StandardCharsets.UTF_8)) {
            int count = toInt(token(in), name, "la cantidad de rutas");
            for (int i = 1; i <= count; i++) {
                String origin = Graph.normalizeCode(token(in));
                String destination = Graph.normalizeCode(token(in));
                int minutes = toInt(token(in), name, "los minutos de la ruta " + i);
                int cost = toInt(token(in), name, "el costo de la ruta " + i);
                try {
                    graph.addRoute(new Route(origin, destination, minutes, cost));
                } catch (IllegalArgumentException e) {
                    throw new DataLoadException("Archivo " + name + ", ruta " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataLoadException("No se pudo abrir el archivo " + name + ": " + e.getMessage());
        } catch (NoSuchElementException e) {
            throw new DataLoadException("El archivo " + name + " termina antes de lo esperado (faltan datos)");
        }
    }

    // Algunos editores de Windows agregan una marca BOM al inicio del archivo
    private static String token(Scanner in) {
        return in.next().replace(String.valueOf((char) 0xFEFF), "");
    }

    private static int toInt(String text, String fileName, String what) throws DataLoadException {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new DataLoadException("Archivo " + fileName + ": se esperaba un numero entero en " + what + " y se encontro '" + text + "'");
        }
    }

    private static double toDouble(String text, String fileName, String what) throws DataLoadException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new DataLoadException("Archivo " + fileName + ": se esperaba un numero en " + what + " y se encontro '" + text + "'");
        }
    }

    private static String fileName(Path file) {
        return file.getFileName() == null ? file.toString() : file.getFileName().toString();
    }
}
