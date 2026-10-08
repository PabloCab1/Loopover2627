# Spec: `Estado` — modelo del tablero 4×4

> **Tarea:** 1 · **Archivos:** `src/Estado.java` (+ `src/Sucesor.java`, consumido por `verify`)
> **Estado:** ✅ Aprobada (Tarea 1 implementada y verificada) · **Última actualización:** 2026-10-08
> **Derivada de:** Javadoc y TODO del esqueleto de `Estado.java`

## 1. Contexto y objetivo

Representar un estado del puzzle Loopover 4×4 de forma compacta y operable con
bitwise, generar sus sucesores y aplicar acciones. Es la base de las Tareas 2 y 3:
sin `Estado` funcional no hay búsqueda ni heurísticas.

## 2. Alcance

**Incluido:** representación, consultas, sucesores, aplicación de acciones,
codificación texto↔código de acciones, `equals`/`hashCode`/`toString`, validaciones.

**No incluido:** búsqueda (Tarea 2), heurísticas (Tarea 3), E/S de ficheros.

## 3. Modelo de datos

### 3.1 Bitboard

- Tablero 4×4 embebido en un `long` sin signo: **16 nibbles de 4 bits**.
- La casilla `i` (fila `i / 4`, columna `i % 4`) ocupa los bits `[i*4, i*4+3]`;
  la casilla 0 queda en los bits más bajos.
- Cada nibble almacena la **ficha** que ocupa la casilla, valor `0..15`.
- Estado resuelto: ficha `i` en casilla `i` →
  `BITBOARD_RESUELTO = 0xFEDCBA98_76543210L`.

```
Casilla:   0  1  2  3     (fila 0)   bits  0-15
           4  5  6  7     (fila 1)   bits 16-31
           8  9 10 11     (fila 2)   bits 32-47
          12 13 14 15     (fila 3)   bits 48-63
```

Invariantes: las 16 fichas son distintas y todas ∈ `[0, 16)`.

### 3.2 Codificación de acción (5 bits)

| Bits | Campo |
|---|---|
| 0-1 | fila `0..3` |
| 2-3 | columna `0..3` |
| 4 | signo: `1` = `+`, `0` = `-` |

- Con `+`: la fila se desplaza a la derecha **y** la columna hacia abajo.
- Con `-`: la fila se desplaza a la izquierda **y** la columna hacia arriba.
- Una acción es **encadenada**: primero se desplaza la fila, después la columna
  (el orden importa, ver §5.2).
- Tabla `ACCIONES[32]` (ya inicializada, no modificar): `ACCIONES[0..15]` con
  signo `+` (`posicion = fila*4 + columna`), `ACCIONES[16..31]` con `-`.

## 4. Contrato de API

| Método | Semántica | Errores |
|---|---|---|
| `Estado(long)` | Directo desde bitboard ya validado | — (asume validado) |
| `Estado(String)` | 32 dígitos: 16 pares, casilla `i` = par `2i..2i+1` | longitud ≠ 32, par fuera de `00..15`, ficha duplicada → `IllegalArgumentException` |
| `Estado(int[])` | `fichas[i]` = ficha en casilla `i`, longitud 16 | longitud ≠ 16, valor fuera de rango, duplicado → `IllegalArgumentException` |
| `long bitboard()` | Devuelve el bitboard interno (para `Visitados`) | — |
| `int ficha(int casilla)` | `(bitboard >>> (i*4)) & 15` | casilla ∉ `[0,16)` → `IllegalArgumentException` |
| `int ficha(int fila, int col)` | Delega en `ficha(fila*4 + columna)` | idem |
| `boolean esResuelto()` | `bitboard == BITBOARD_RESUELTO` | — |
| `List<Sucesor> sucesores()` | 32 sucesores, uno por cada `ACCIONES[i]`, coste `1.0f` | — |
| `Estado aplicar(int accion)` | Nuevo estado; **no** muta el actual | acción ∉ `[0,31]` → `IllegalArgumentException` |
| `static String accionComoTexto(int)` | `"" + fila + col + signo` → `"01+"`, `"33-"` | acción fuera de rango → `IllegalArgumentException` |
| `static int accionDesde(String)` | Inverso de lo anterior | longitud ≠ 3, dígitos no ∈ `0..3`, signo no ∈ `+-` → `IllegalArgumentException` |
| `boolean equals(Object)` / `int hashCode()` | Igualdad y hash por bitboard | `hashCode = Long.hashCode(bitboard)` |
| `String toString()` | **32 dígitos**, cada ficha en dos dígitos con cero inicial si < 10 | — |

Formato `toString` (y de entrada de `Estado(String)`), estado resuelto:
`"00010203040506070809101112131415"`.

## 5. Algoritmos

### 5.1 Construcción del bitboard

```
resultado = 0
para cada i en 0..15:
    validar fichas[i] ∈ [0,16) y sin duplicados
    resultado |= ((long) fichas[i]) << (i * 4)
```

### 5.2 `aplicar(accion)`

1. `fila    = accion & 0b00011`
2. `columna = (accion & 0b01100) >>> 2`
3. `positivo = (accion & 0b10000) != 0`
4. `tmp  = desplazarFila(bitboard, fila, positivo)`
5. `dev = desplazarColumna(tmp, columna, positivo)`

### 5.3 `desplazarFila(bb, fila, positivo)` — rotación circular

```
mascaraFila    = 0xFFFF      << (fila * 16)
mascaraRetorno = 0xF         << (fila * 16)
extraida       = bb & mascaraFila

positivo (derecha):
    rotada = ((extraida << 4) & mascaraFila) | ((extraida >>> 12) & mascaraRetorno)
negativo (izquierda):
    mascaraTope = mascaraRetorno << 12
    rotada = ((extraida >>> 4) & mascaraFila) | ((extraida << 12) & mascaraTope)

resultado = (bb & ~mascaraFila) | rotada
```

### 5.4 `desplazarColumna(bb, col, positivo)` — rotación circular

```
mascaraColumna = 0x000F000F000F000F << (col * 4)
mascaraRetorno = 0xF               << (col * 4)
extraida       = bb & mascaraColumna

positivo (abajo):
    rotada = ((extraida << 16) & mascaraColumna) | ((extraida >>> 48) & mascaraRetorno)
negativo (arriba):
    mascaraSuperior = 0xF000000000000000L >>> (12 - col * 4)
    rotada = ((extraida >>> 16) & mascaraColumna) | ((extraida << 48) & mascaraSuperior)

resultado = (bb & ~mascaraColumna) | rotada
```

## 6. Casos límite y errores

| Caso | Entrada | Resultado esperado |
|---|---|---|
| Cadena corta/larga | `"0001"` (≠32) | `IllegalArgumentException` |
| Ficha fuera de rango | par `"16"` | `IllegalArgumentException` |
| Ficha duplicada | dos pares `"05"` | `IllegalArgumentException` |
| Array de 15 fichas | `new int[15]` | `IllegalArgumentException` |
| Acción negativa / >31 | `aplicar(-1)`, `aplicar(32)` | `IllegalArgumentException` |
| Acción texto malformada | `"0X+"`, `"011"`, `"40+"` | `IllegalArgumentException` |
| Casilla fuera de rango | `ficha(16)`, `ficha(-1)` | `IllegalArgumentException` |
| Estado resuelto | `"00010203040506070809101112131415"` | `esResuelto() == true` |

## 7. Criterios de aceptación

- **CA-1.1**: `new Estado("00010203040506070809101112131415").esResuelto()` es `true`.
- **CA-1.2**: `toString()` del estado resuelto devuelve exactamente esa cadena de 32 dígitos, y `new Estado(estado.toString()).equals(estado)` (round-trip).
- **CA-1.3**: `ficha(i)` devuelve el valor del nibble `i`; `ficha(i/4, i%4)` coincide con `ficha(i)` para todo `i`.
- **CA-1.4**: `bitboard()` del estado resuelto es `0xFEDCBA98_76543210L`.
- **CA-1.5**: `sucesores()` devuelve exactamente 32 elementos, con acciones `ACCIONES[0..31]`, coste `1.0f` y estados distintos del original.
- **CA-1.6**: para cualquier acción, `aplicar(a)` no modifica el estado receptor
  (inmutabilidad). Además **toda acción tiene orden 7**: aplicar la misma acción
  7 veces seguidas devuelve el estado original (`a⁷ = identidad`), por lo que la
  inversa de `a` es aplicarla **6 veces más**. La acción con el signo invertido
  **no** es la inversa (ver D4).
- **CA-1.7**: `00+` desplaza la fila 0 una casilla a la derecha con retorno circular: las fichas de la fila pasan `0→1→2→3→0`.
- **CA-1.8**: `00+` sobre una fila afectada por columna: la columna se desplaza **después** de la fila (comprobar con un tablero asimétrico donde fila y columna se solapen en la casilla (0,0)).
- **CA-1.9**: `accionComoTexto` y `accionDesde` son inversas para las 32 acciones: `accionDesde(accionComoTexto(a)) == a` para `a ∈ [0,31]`.
- **CA-1.10**: `accionComoTexto(ACCIONES[0])` = `"00+"`, y `ACCIONES[16]` = código de `"00-"`.
- **CA-1.11**: `equals` es `true` solo para estados con el mismo bitboard; `hashCode` es `Long.hashCode(bitboard)` y es estable entre llamadas.
- **CA-1.12**: toda violación de validación lanza `IllegalArgumentException` con mensaje que incluye el valor recibido.
- **CA-1.13**: `verify -s <resuelto>` lista 32 sucesores sin error; `verify -s <resuelto> -a "00+"` imprime el estado con la fila 0 rotada.

## 8. Estrategia de verificación

- Tests unitarios JUnit 5 en `src/test/EstadoTest.java` (30 tests, uno por CA o
  grupo de CA): ejecutar con `mvn test`.
- Manual: `mvn -q package` y `java -jar target/loopover.jar verify ...` con
  estados conocidos (resuelto, tablero asimétrico, una sola acción).

## 9. Decisiones y supuestos

- **D1**: inmutabilidad — `aplicar` devuelve un `Estado` nuevo (necesario para
  `Visitados` y para reutilizar nodos). Decidido por el esqueleto (`final long bitboard`).
- **D2**: el coste de toda acción es `1.0f` (métrica de movimientos unitarios).
- **D4**: una acción encadenada afecta a las 7 casillas de su fila ∪ su columna
  (se solapan en la casilla (fila, columna)) y actúa como **un ciclo de orden 7**
  sobre ellas; las 9 casillas restantes quedan fijas. Corregido el 2026-10-08
  tras verificación empírica: la acción con signo invertido **no** compone a
  identidad (fila y columna no conmutan en la casilla compartida).
- **A1**: el estado de entrada al CLI siempre tiene las 16 fichas distintas
  (el puzzle no tiene piezas repetidas); se valida igualmente.

## 10. Referencias

- [`spec-busqueda.md`](spec-busqueda.md) — consume `sucesores()` y `aplicar()`.
- [`referencias.md`](../referencias.md) — notación de movimientos en otros solvers.
