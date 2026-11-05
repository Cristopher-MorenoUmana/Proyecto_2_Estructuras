# Rutas aereas - EIF207 Estructuras de Datos, Proyecto II

Planificador de vuelos entre aeropuertos de America (grafo dirigido y ponderado con lista de adyacencia).

## Datos tecnicos

| Concepto | Valor |
|---|---|
| Lenguaje | Java 17 o posterior |
| Biblioteca grafica | JavaFX (Maven la descarga sola) |
| Entorno | Maven 3.9 o posterior (IntelliJ IDEA, NetBeans o Eclipse) |

## Como compilar y ejecutar

```
mvn clean compile
mvn javafx:run
```

## Datos de entrada

Los archivos de la carpeta `data/` se leen al iniciar:

- `aeropuertos.txt`: primera linea con la cantidad; luego `codigo x y ciudad pais`
- `rutas.txt`: primera linea con la cantidad; luego `origen destino minutos costo`

_En construccion._
