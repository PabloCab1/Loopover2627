# Spec: `Busqueda` y estructuras del árbol de búsqueda

> **Tarea:** 2 · **Archivos:** `src/Busqueda.java`, `src/Nodo.java`, `src/Frontera.java`, `src/Visitados.java`
> **Estado:** 📝 Borrador · **Última actualización:** 2026-10-08
> **Depende de:** [`spec-estado.md`](spec-estado.md) (Tarea 1 implementada)
> **Derivada de:** esqueleto de las 4 clases + `ComandoBusqueda.java` (CLI)

## 1. Contexto y objetivo

Motor de búsqueda genérico sobre el espacio de estados definido por `Estado`:
expande el mejor nodo de la frontera hasta encontrar la solución, soportando 6
estrategias, límites de profundidad/memoria/visitados y recolectando métricas.

## 2. Alcance

**Incluido:** árbol de nodos, frontera (heap), tabla de visitados, las 6
estrategias, límites y métricas, reconstrucción del camino.

**No incluido:** heurísticas informadas de verdad (Tarea 3; aquí llegan como
`ToIntFunction<Estado>` inyectado), poda de árbol por memoria (Tarea 3,
`eliminarPeorHojaNoRaiz`).

## 3. API y semántica

### 3.1 `Busqueda`

| Método | Semántica |
|---|---|
| `Busqueda(estado, estrategia, profMax)` | Límites por defecto: `maxNodosArbol = 10_000_000`, `maxVisitados = 0` (ilimitado), heurística = `h → 0` |
| `Busqueda(..., maxNodosArbol)` | Capacidad del árbol (SMA*); también válida para las demás estrategias |
| `Busqueda(..., heuristica)` | Heurística usada por `VORAZ` y `A_ESTRELLA` (las demás la ignoran) |
| `Busqueda(..., maxVisitados)` | `> 0` aborta la búsqueda al alcanzar ese número de estados visitados |
| `Nodo buscar()` | Ejecuta la búsqueda; **solución** o `null` (sin solución / límite) |
| `int nodosExpandidos()` | Nodos cuyos sucesores se generaron |
| `int estadosVisitados()` | Tamaño de la tabla `Visitados` tras `buscar()` |
| `long tiempoMs()` | Tiempo de pared de la última ejecución de `buscar()` |
| `boolean limiteVisitadosAlcanzado()` | `true` si `buscar()` terminó por `maxVisitados` |

Llamadas a métricas antes de `buscar()` devuelven `0`.

### 3.2 `Nodo`

| Método | Semántica |
|---|---|
| `Nodo(estado)` | Raíz: `padre = null`, `accion = -1`, `profundidad = 0`, `costoAcumulado = 0` |
| `Nodo(padre, accion)` | Hereda estado resultante de `padre.estado().aplicar(accion)`, coste de acción `1.0f` |
| `Nodo(padre, accion, costoAccion)` | Igual anterior con coste explícito (permite costes no uniformes) |
| `profundidad()` | `padre.profundidad() + 1` (raíz = 0) |
| `costoAcumulado()` | `g(n)` = suma de costes de la ruta raíz→nodo |
| `fijarHeuristica(int)` / `valor()` / `fijarValor(int)` | `h(n)` y `f(n)` cacheados en el nodo |
| `compareTo(Nodo)` | Orden del heap de la frontera: por `valor` (`f` o `h` según estrategia); desempate estable (ver D3) |
| `camino()` | `ObjectList<Nodo>` desde la raíz hasta este nodo (incluido) |
| `caminoComoTexto()` | Acciones del camino separadas por `,` → `"21+,03-,10+"`; raíz sin acción no aporta texto |
| `esRaiz()` | `padre == null` |
| `equals`/`hashCode` | Por identidad de estado+profundidad+acción (nodos distintos pueden compartir estado) |

**Respaldo SMA\* (usado por `A_ESTRELLA_ACOTADA`, sin implementar hasta Tarea 3):**

| Método | Semántica |
|---|---|
| `restarHijo()` | Descuenta un hijo no generado/expandido |
| `reexpandir()` | El nodo vuelve a estar disponible para expansión |
| `marcarExpandido()` / `completado()` | Todos sus sucesores han sido generados |
| `agotado()` | Sin hijos posibles en memoria (bloqueado por la capacidad) |
| `respaldarMinimo(int v)` | Propaga hacia arriba el mejor `f` conocido del subárbol |

### 3.3 `Frontera`

Heap dirigido de hasta `maxNodos` nodos.

| Método | Semántica |
|---|---|
| `Frontera(int maxNodos)` | Capacidad fija |
| `tamano()` / `vacia()` | Tamaño / si está vacía |
| `contiene(Nodo)` | Pertenencia (para no reinsertar) |
| `insertar(Nodo)` | Inserta manteniendo el orden de `compareTo` |
| `extraerMejor()` | Extrae el mínimo según `compareTo`; `null` si vacía |
| `eliminarPeorHojaNoRaiz()` | Extrae el **peor** nodo hoja distinto de la raíz (SMA*); `null` si no hay elegible. **Implementación: Tarea 3.** |

### 3.4 `Visitados`

| Método | Semántica |
|---|---|
| `registrarSiMejor(long estado, float valor)` | Devuelve `true` (registra) si `estado` no estaba, o si estaba con un valor **peor** (mayor) que `valor`; `false` si ya existe un valor igual o mejor |
| `tamano()` | Número de estados distintos registrados |

Clave = `Estado.bitboard()`, valor = mejor `f` (o `g`/`h` según estrategia) visto.

## 4. Estrategias

| Estrategia | Orden de extracción | Usa heurística | Coste |
|---|---|---|---|
| `PROFUNDIDAD` | LIFO (pila) — el nodo más profundo/reciente primero | no | — |
| `ANCHURA` | FIFO (cola) | no | — |
| `COSTO_UNIFORME` | mínimo `g(n)` | no | — |
| `VORAZ` | mínimo `h(n)` | sí | — |
| `A_ESTRELLA` | mínimo `f(n) = g(n) + h(n)` | sí | óptimo si `h` admisible |
| `A_ESTRELLA_ACOTADA` | mínimo `f(n)` con capacidad `maxNodosArbol` (SMA*); ante desbordamiento elimina la peor hoja y re-expande | sí | óptimo si `h` admisible y capacidad suficiente |

Reglas comunes:

- El primer estado en `esResuelto()` que se **extrae de la frontera** es la solución.
- Con heurística `CERO`, `A_ESTRELLA` equivale a `COSTO_UNIFORME` (costes unitarios)
  y exploración en anchura efectiva; así lo describe la ayuda de la CLI.
- `profundidadMaxima` corta la generación de sucesores (no se expande un nodo con
  `profundidad() >= profundidadMaxima`), aplicable a todas las estrategias.
- Si `maxVisitados > 0` y se supera, `buscar()` devuelve `null` y
  `limiteVisitadosAlcanzado()` es `true` (mensaje de error específico en la CLI).
- `A_ESTRELLA_ACOTADA` sin `-c` la rechaza la CLI antes de construir `Busqueda`.

## 5. Casos límite

| Caso | Resultado esperado |
|---|---|
| Estado inicial ya resuelto | La CLI lo detecta antes de buscar (`"(ya resuelto)"`); si llega a `buscar()`, devuelve la raíz |
| Sin solución dentro de `profundidadMaxima` | `buscar()` → `null`, `limiteVisitadosAlcanzado()` → `false` |
| `maxVisitados` alcanzado | `buscar()` → `null`, `limiteVisitadosAlcanzado()` → `true` |
| Frontera vacía sin solución (espacio agotado) | `buscar()` → `null` |
| Heurística inyectada `null` | Solo válida en estrategias no informadas; si es obligatoria (VORAZ/A*), `IllegalArgumentException` |

## 6. Criterios de aceptación

- **CA-2.1**: con `PROFUNDIDAD`, `ANCHURA` y `COSTO_UNIFORME`, el resultado es una
  secuencia de acciones que, aplicada desde el estado inicial con `aplicar`,
  alcanza un estado con `esResuelto() == true`.
- **CA-2.2**: con `COSTO_UNIFORME`, la solución devuelta tiene longitud ≤ la de
  cualquier otra estrategia encontrada para el mismo estado (subóptimo aceptable
  solo en `PROFUNDIDAD`/`VORAZ`).
- **CA-2.3**: con `A_ESTRELLA` + heurística admisible, la longitud de la solución
  es óptima (igual que `COSTO_UNIFORME`).
- **CA-2.4**: `nodosExpandidos()`, `estadosVisitados() > 0` y `tiempoMs() >= 0`
  tras un `buscar()` con solución; `0` antes de ejecutar.
- **CA-2.5**: `limiteVisitadosAlcanzado()` es `false` en búsqueda normal y `true`
  exactamente cuando el aborto por `-m` es la causa del `null`.
- **CA-2.6**: `caminoComoTexto()` produce acciones parseables por
  `Estado.accionDesde` separadas por `,`, y aplicarlas reproduciendo el nodo
  solución lleva del inicial al resuelto (round-trip).
- **CA-2.7**: la raíz tiene `profundidad() == 0`, `costoAcumulado() == 0`,
  `padre() == null`, `esRaiz() == true`; cada hijo satisface
  `profundidad() == padre().profundidad() + 1` y
  `costoAcumulado() == padre().costoAcumulado() + costoAccion`.
- **CA-2.8**: un mismo estado nunca ocupa dos entradas de `Visitados`;
  `registrarSiMejor` devuelve `false` cuando el valor no mejora.
- **CA-2.9**: con `-c N` en `A_ESTRELLA_ACOTADA`, la frontera nunca supera `N`
  nodos y `buscar()` sigue devolviendo una solución válida o `null` documentado.
- **CA-2.10**: CLI `solve -s <estado> -e <estrategia>` imprime el camino y, con
  `-v`, `profundidad`, `nodosExpandidos`, `estadosVisitados` y `tiempo` coherentes.

## 7. Estrategia de verificación

- Casos pequeños y baratos primero: estados a 1-3 movimientos de la solución
  (la óptima debe tener esa longitud exacta).
- Comparación cruzada entre estrategias sobre el mismo estado (CA-2.2/2.3).
- CLI: `java -jar target/loopover.jar solve -s ... -e COSTO_UNIFORME -v`.

## 8. Decisiones y supuestos

- **D1**: la solución se reconoce al **extraer** el nodo de la frontera (no al
  insertarlo), evitando expandir un estado resuelto.
- **D2**: `Visitados` guarda el mejor valor visto; permite podar reexpansiones
  peores sin perder optimalidad en A* (monotonía de `f`).
- **D3**: desempate de `compareTo` — pendiente de fijar (propuesta: menor
  profundidad primero para rutas más cortas en empates de `f`); fijarlo en la
  spec antes de implementar.
- **A1**: coste por defecto de acción `1.0f`; el constructor con coste explícito
  existe para futuros costes variables, hoy todos valen 1.
- **A2**: `Frontera` y `Visitados` no dependen de la estrategia: el orden es
  siempre `Nodo.compareTo`; la estrategia decide cómo se calcula `valor`.

## 9. Referencias

- [`spec-estado.md`](spec-estado.md) — contrato de `aplicar`/`sucesores`.
- [`spec-driven-development.md`](../spec-driven-development.md) — flujo de cierre.
