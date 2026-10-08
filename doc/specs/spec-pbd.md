# Spec: Heurísticas — `PBD`, `Heuristicas`, `HeuristicasPBD`

> **Tarea:** 3 · **Archivos:** `src/PBD.java`, `src/Heuristicas.java`, `src/HeuristicasPBD.java`
> **Estado:** 📝 Borrador · **Última actualización:** 2026-10-08
> **Depende de:** [`spec-estado.md`](spec-estado.md) (Tarea 1) · consumido por [`spec-busqueda.md`](spec-busqueda.md) (Tarea 2)

## 1. Contexto y objetivo

Aportar funciones heurísticas `h(estado) → int` para las estrategias informadas
de la Tarea 2: dos Manhattan geométricas, una por permutaciones y dos basadas en
pattern databases de 8 piezas (`PBD`), más su composición (máximo / suma).

## 2. Alcance

**Incluido:** carga y consulta de las PBD, heurísticas geométricas y de
permutaciones, composiciones admisibles/no admisibles, gestión del recurso
`.dat`.

**No incluido:** generación de las PBD (herramienta externa; los `.dat` se
ignoran en git), cambios en el motor de búsqueda.

## 3. API

### 3.1 `PBD` — pattern database de 8 piezas

| Método | Semántica |
|---|---|
| `PBD.TAMANNO = 32_432_400` | Número de entradas de la tabla (byte/entrada ≈ 32 MB) |
| `PBD(String nombreFichero)` | Carga el fichero; `minimo = 0` |
| `PBD(String nombreFichero, int minimo)` | Carga con valor mínimo desplazado (ver Q1) |
| `static PBD desdeRecurso(String nombreRecurso, int minimo)` | Carga desde recurso de classpath (p. ej. `"/pdb8.dat"`) |
| `int valor(long tablero)` | Consulta por bitboard directo |
| `int valor(Estado estado)` | Delega en `valor(estado.bitboard())` |
| `int minimo()` | Devuelve el `minimo` configurado |

Invariantes: `valor ≥ 0` para cualquier tablero; `valor(bitboardResuelto) = 0`
(con `minimo = 0`); lectura sin bloquear tras la carga.

### 3.2 `Heuristicas` — geometría toroidal

| Método | Semántica | Admisibilidad |
|---|---|---|
| `heuristicaManhattanToroidal(Estado)` | Manhattan en toro 4×4, heurística **por defecto** de la CLI | pendiente de argumentar (Q2) |
| `heuristicaManhattanAdmisible(Estado)` | Cota inferior garantizada | **admisible** |
| `heuristicaPermutaciones(Estado)` | Cota por desordenamiento de permutaciones | pendiente de argumentar (Q2) |

Todas devuelven `int ≥ 0`, `0` en el estado resuelto. Hoy devuelven `0` (stub
válido para Tarea 2 con estrategias no informadas).

### 3.3 `HeuristicasPBD` — composición

Recursos: `RECURSO_PARES = "/pdb8.dat"`, `RECURSO_IMPARES = "/pdb8_impares.dat"`.

| Método | Semántica | Admisibilidad |
|---|---|---|
| `HeuristicasPBD()` | Carga ambas PBD (`IOException` si falta alguna) | — |
| `valorPares(Estado)` | Consulta a la PBD de piezas pares | admisible |
| `valorImpares(Estado)` | Consulta a la PBD de piezas impares | admisible |
| `valorMaximo(Estado)` | `max(pares, impares)` → tipo `PBD_8` | **admisible** (máx. de admisibles) |
| `valorSuma(Estado)` | `pares + impares` → tipo `PBD_8_SUMA` | **NO admisible** ("admisible falso": no garantiza solución óptima) |
| `funcion(Tipo)` | Devuelve la `ToIntFunction<Estado>` correspondiente | según `Tipo` |

`Tipo`: `PARES_8`, `IMPARES_8`, `PBD_8` (= máx.), `PBD_8_SUMA` (= suma).

Equivalencia con la CLI (`ComandoBusqueda`):

| `-h` | Implementación |
|---|---|
| `MANHATTAN` (defecto) | `Heuristicas::heuristicaManhattanToroidal` |
| `CERO` | `estado -> 0` |
| `MANHATTAN_ADMISIBLE` | `Heuristicas::heuristicaManhattanAdmisible` |
| `PERMUTACIONES` | `Heuristicas::heuristicaPermutaciones` |
| `PARES_8` / `IMPARES_8` / `PBD_8` / `PBD_8_SUMA` | `new HeuristicasPBD().funcion(...)` |

## 4. Diseño de la indexación (Tarea 3)

- El índice de la PBD mapea bitboard → `[0, TAMANNO)` de forma **biyectiva sobre
  el patrón** (las 8 piezas del patrón entre las 16 casillas, sin distinguir el
  resto).
- Aritmética de control: `TAMANNO = 32_432_400 = 9·10·11·12·13·14·15 = 15!/8!`
  (identidad útil al diseñar el código Lehmer / selección ordenada).
- La codificación debe ser exacta: un índice mal posicionado produce valores
  plausibles pero inválidos — exigir test de regresión (ver CA-3.4).

## 5. Casos límite y errores

| Caso | Resultado esperado |
|---|---|
| Fichero `.dat` inexistente o corrupto | `IOException` en el constructor |
| `.dat` con longitud ≠ `TAMANNO` | `IOException` con el tamaño esperado/obtenido |
| Tablero con patrón parcial | devuelve cota ≥ 0; nunca lanza |
| Dos PBD en `HeuristicasPBD` | fallo de carga de cualquiera aborta la construcción |

## 6. Criterios de aceptación

- **CA-3.1**: `new HeuristicasPBD()` carga ambas PBD y `funcion(Tipo)` devuelve
  funciones sin excepción para los 4 tipos; con `CERO`/stub `0` la Tarea 2 no se
  ve afectada.
- **CA-3.2**: `valorMaximo(e) == max(valorPares(e), valorImpares(e))` y
  `valorSuma(e) == valorPares(e) + valorImpares(e)` para cualquier estado.
- **CA-3.3**: `PBD_8` es admisible: para estados con solución de longitud `k`,
  `valorMaximo ≤ k` (verificar sobre una muestra de estados con solución óptima
  conocida). `PBD_8_SUMA` **no** se afirma admisible y así se documenta en la CLI.
- **CA-3.4**: test de indexación — para una muestra de estados, reindexar el
  patrón extraído del bitboard reproduce el mismo índice en ida y vuelta.
- **CA-3.5**: las tres heurísticas de `Heuristicas` devuelven `0` en el estado
  resuelto y `≥ 0` siempre; `heuristicaManhattanAdmisible` jamás supera el coste
  óptimo de la muestra verificada.
- **CA-3.6**: falta de recurso → `IOException` mensajera; la CLI reporta
  `"Error de configuración: ..."` y sale con código 1.
- **CA-3.7**: rendimiento — `valor()` constante O(1) por consulta (acceso a
  array, sin E/S); medir que A* + `PBD_8` expande **menos** nodos que
  A* + `MANHATTAN` para el mismo estado.

## 7. Estrategia de verificación

- Generador/verificador de la PBD fuera del repo (los `.dat` van ignorados en git).
- Muestra de estados con longitud óptima conocida (resueltos con
  `COSTO_UNIFORME` a profundidad baja) para los tests de admisibilidad.
- Comparativa A*: `-h MANHATTAN` vs `-h PBD_8` con `-v` (nodos expandidos).

## 8. Decisiones y preguntas abiertas

- **Q1**: significado exacto del parámetro `minimo` (¿valor mínimo almacenado
  del que se resta al guardar y se suma al leer?). **Confirmar con el generador
  del `.dat` antes de implementar**; `minimo()` expone el valor.
- **Q2**: fórmulas concretas de `heuristicaManhattanToroidal` y
  `heuristicaPermutaciones` + su argumento de admisibilidad. Referencia de partida:
  en Loopover una acción mueve fila **y** columna, así que cada ficha se acerca
  a su destino en al menos uno de los dos ejes por movimiento — documentar el
  reparto y el divisor en la propia función.
- **Q3**: ¿`PBD_8_SUMA` se usa sabiendo que rompe optimalidad? Sí — la CLI ya lo
  advierte; mantener ese aviso al implementar.
- **D1**: las PBD son artefactos generados: `.dat` fuera de git (ver `.gitignore`),
  el repo solo transporta el código.

## 9. Referencias

- [`referencias.md`](../referencias.md) — torchlight/loopsolver (`heuristics.md`
  documenta PDBs de 4×4/5×5), generadores de PDB estilo Korf/Felner.
- [`spec-busqueda.md`](spec-busqueda.md) — contrato `ToIntFunction<Estado>`.
