package cr.ac.una.flightplanner;

import cr.ac.una.flightplanner.io.DataLoadException;
import cr.ac.una.flightplanner.io.DataLoader;
import cr.ac.una.flightplanner.model.Airport;
import cr.ac.una.flightplanner.model.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataLoaderTest {

    @Test
    void loadsTheProvidedFiles() throws Exception {
        Graph graph = DataLoader.load(Path.of("data/aeropuertos.txt"), Path.of("data/rutas.txt"));
        Airport sjo = graph.getAirport("sjo");
        assertEquals("SJO", sjo.code());
        assertEquals(464, sjo.x());
        assertEquals(447, sjo.y());
        assertEquals("San Jose, Costa Rica", sjo.displayName());
        assertEquals(7, graph.getOutgoingRoutes("SJO").size());
    }

    @Test
    void missingFileGivesUserMessage() {
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> DataLoader.load(Path.of("no-existe/aeropuertos.txt"), Path.of("no-existe/rutas.txt")));
        assertTrue(e.getMessage().contains("aeropuertos.txt"));
    }

    @Test
    void routeWithUnknownAirportIsReported() throws IOException {
        Path folder = Files.createTempDirectory("rutas-test");
        Files.writeString(folder.resolve("aeropuertos.txt"), "1\nAAA 10 20 Ciudad Pais\n");
        Files.writeString(folder.resolve("rutas.txt"), "1\nAAA ZZZ 5 5\n");
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> DataLoader.load(folder.resolve("aeropuertos.txt"), folder.resolve("rutas.txt")));
        assertTrue(e.getMessage().contains("ZZZ"));
    }

    @Test
    void truncatedFileIsReported() throws IOException {
        Path folder = Files.createTempDirectory("rutas-test");
        Files.writeString(folder.resolve("aeropuertos.txt"), "2\nAAA 10 20 Ciudad Pais\n");
        Files.writeString(folder.resolve("rutas.txt"), "0\n");
        assertThrows(DataLoadException.class,
                () -> DataLoader.load(folder.resolve("aeropuertos.txt"), folder.resolve("rutas.txt")));
    }

    @Test
    void textWhereANumberIsExpectedIsReported() throws IOException {
        Path folder = Files.createTempDirectory("rutas-test");
        Files.writeString(folder.resolve("aeropuertos.txt"), "1\nAAA diez 20 Ciudad Pais\n");
        Files.writeString(folder.resolve("rutas.txt"), "0\n");
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> DataLoader.load(folder.resolve("aeropuertos.txt"), folder.resolve("rutas.txt")));
        assertTrue(e.getMessage().contains("diez"));
    }
}
