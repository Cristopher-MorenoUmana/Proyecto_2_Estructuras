# Rutas aereas - EIF207 Estructuras de Datos, Proyecto II

Planificador de vuelos entre aeropuertos de America. Cada aeropuerto es un vertice de un grafo dirigido y
ponderado (lista de adyacencia); cada ruta tiene dos pesos: minutos y costo en dolares.

## Integrantes

- Cristopher Moreno Umaña
- Adrian Arce Gomez

## Datos tecnicos

| Concepto | Valor |
|---|---|
| Lenguaje | Java 17 o posterior |
| Biblioteca grafica | JavaFX 21 (Maven la descarga sola) |
| Entorno | Maven 3.9 o posterior, desde cualquier IDE que lo soporte (IntelliJ IDEA, NetBeans, Eclipse) |
| Pruebas | JUnit 6 |

## Como compilar y ejecutar

```
mvn clean compile
mvn javafx:run
```

Para usar otra carpeta de datos (por defecto se usa `data/`):

```
mvn javafx:run -Djavafx.args="ruta/a/otra/carpeta"
```

Desde el IDE se ejecuta la clase `cr.ac.una.flightplanner.Main`; el primer argumento es la carpeta de datos.
Tambien se puede cambiar de carpeta con el boton "Cargar otros archivos..." de la ventana.

Pruebas automaticas: `mvn test`

## Datos de entrada

La carpeta de datos debe tener dos archivos de texto con campos separados por espacios (los nombres usan `_`):

- `aeropuertos.txt`: primera linea con la cantidad; luego `codigo x y ciudad pais`
- `rutas.txt`: primera linea con la cantidad; luego `origen destino minutos costo`

El programa no tiene datos escritos en el codigo: todo se lee de estos archivos.

## Estructura del codigo

```
src/main/java/cr/ac/una/flightplanner
  Main.java                 punto de entrada
  model/                    Airport, Route (records) y Graph (lista de adyacencia, cancelar y restaurar rutas)
  io/                       DataLoader (lee los archivos) y DataLoadException
  algorithm/                BreadthFirstSearch, Dijkstra, DepthFirstSearch, RouteFinder, PathResult, Criterion
  ui/                       App (ventana), MapPane (mapa), AirportNode, RouteEdge, FlightAnimator (avion), ResultFormatter
src/test/java/...           pruebas (grafo pequeno, archivos reales contra Floyd-Warshall, lectura de archivos)
data/                       archivos de datos entregados
docs/informe.pdf            informe: estructuras, complejidad y respuestas de la seccion 8
```

Los paquetes `model`, `io` y `algorithm` no dependen de JavaFX; solo `ui` lo usa.

## Uso de la ventana

- Clic en un aeropuerto: muestra sus datos y rutas salientes, y lo usa como origen y luego como destino.
  Tambien se pueden escribir los codigos; "Consultar aeropuerto (origen)" muestra los datos del codigo escrito.
- Criterio: menos escalas (BFS), mas rapida o mas barata (Dijkstra con minutos o con dolares).
  La ruta queda resaltada en azul y el panel muestra la secuencia, los vuelos, los minutos y el costo.
- Al encontrar una ruta, un avion la recorre tramo por tramo y se detiene un momento en cada escala.
  El recuadro naranja muestra el tramo actual y los minutos y el costo acumulados.
  "Pausar / continuar" y "Reiniciar vuelo" controlan la animacion.
- "Alcanzables desde el origen" y "Desde donde se llega al destino" (DFS): verde = si, gris = no.
- Cancelar una ruta la quita del grafo en memoria (se ve punteada en rojo); "Restaurar" las devuelve todas.
  Los archivos nunca se modifican.

  ## Declaracion de uso de IA

  En este proyecto se uso la inteligencia artificial para depuracion de errores y como ayuda en el desarrollo de la interfaz grafica, dado a que para el proyecto se usan clases "personalizadas", por lo que herramientas como SceneBuilder no ayudan, por lo que habría que posicionar cada elemento mediante coordenadas.
