# Loopover2627

Resolutor del puzzle **Loopover 4x4** mediante búsqueda en espacio de estado con movimientos encadenados (fila + columna por acción). Proyecto Java 17 + Maven con CLI basada en [picocli](https://picocli.info/).

## Compilar y ejecutar

```bash
mvn package
java -jar target/loopover.jar --help
```

### `verify` — validar un estado y aplicar acciones

```bash
# Muestra los 32 sucesores de un estado
java -jar target/loopover.jar verify -s "00010203040506070809101112131415"

# Aplica una lista de acciones (fila+columna+signo)
java -jar target/loopover.jar verify -s "00010203040506070809101112131415" -a "21+,03-"
```

### `solve` — resolver un estado

```bash
java -jar target/loopover.jar solve -s "<32 digitos>" \
  -e A_ESTRELLA -h MANHATTAN -p 1000 -m 50000000 -v
```

| Opción | Descripción |
|---|---|
| `-s` | Estado como 32 dígitos (par por casilla, casilla 0 = posiciones 0-1) |
| `-e` | `PROFUNDIDAD`, `ANCHURA`, `COSTO_UNIFORME`, `VORAZ`, `A_ESTRELLA`, `A_ESTRELLA_ACOTADA` |
| `-p` | Profundidad máxima (defecto 1000) |
| `-c` | Máximo de nodos del árbol en memoria, obligatorio en `A_ESTRELLA_ACOTADA` (SMA*) |
| `-h` | Heurística: `MANHATTAN`, `CERO`, `MANHATTAN_ADMISIBLE`, `PERMUTACIONES`, `PARES_8`, `IMPARES_8`, `PBD_8`, `PBD_8_SUMA` |
| `-m` | Límite de estados visitados antes de abortar (0 = ilimitado) |
| `-v` | Estadísticas de la búsqueda |

La solución se imprime como secuencia de acciones, por ejemplo `21+,03-,10+`.

## Estructura

```
├── src/            Código Java (sin paquetes)
├── doc/            Toda la documentación (.md) — spec-driven development
│   ├── specs/      Especificaciones por tarea (Tarea 1-3)
│   ├── plantilla-spec.md
│   ├── spec-driven-development.md
│   └── referencias.md
├── pom.xml
└── .gitignore
```

## Estado del proyecto

| Tarea | Alcance | Estado |
|---|---|---|
| 1 | `Estado` — bitboard, acciones, sucesores | ✅ Completada |
| 2 | `Busqueda`, `Nodo`, `Frontera`, `Visitados` | ⬜ Pendiente |
| 3 | `PBD`, `Heuristicas`, `HeuristicasPBD` | ⬜ Pendiente |

## Documentación

- [Índice de documentación](doc/README.md)
- [Flujo spec-driven development](doc/spec-driven-development.md)
- [Referencias y repositorios relacionados](doc/referencias.md)
