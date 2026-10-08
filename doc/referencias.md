# Referencias — Loopover y buscadores relacionados

Fuentes consultadas al diseñar este proyecto (2026-10-08). Enlaces externos sin
modificar; ninguno es dependencia del código.

## El puzzle

| Fuente | Qué aporta |
|---|---|
| [loopover.xyz](https://loopover.xyz/) | Juego original (Janis Pritzkau, remake del de carykh): reglas, tamaños, métrica de movimientos |
| [Codewars — Loopover (1 kyu)](https://www.codewars.com/kata/5c1d796370fee68b1e000611) | Enunciado canónico del puzzle con notación de movimientos; kata de referencia con soluciones en Java/Python/C#/Rust |
| [Simon Tatham — Sixteen](https://www.chiark.greenend.org.uk/~sgtatham/puzzles/doc/sixteen.html) | Implementación con **reglas idénticas** (desplazar fila/columna con retorno circular) y discusión de la mecánica de mezcla |

## Solvers (referencia de algoritmos)

| Repositorio | Lenguaje | Relevancia |
|---|---|---|
| [torchlight/loopsolver](https://github.com/torchlight/loopsolver) | Java | **El más afín**: solver 4×4/5×5 con IDA\*; su [`heuristics.md`](https://github.com/torchlight/loopsolver/blob/master/heuristics.md) documenta pattern databases, cosetas dobles y walking distance — modelo a seguir para documentar la Tarea 3. Cota práctica: 4×4 resuelve en ~170 ms con 13,76 movimientos de media |
| [AndreiToroplean/loopover](https://github.com/AndreiToroplean/loopover) | Python | Solver de la kata de Codewars (94 commits) con desarrollo matemático del puzzle (grupos, descomposición de movimientos) |
| [JonathanAlderson/LoopOver](https://github.com/JonathanAlderson/LoopOver) | Python | Solver visual (lectura de pantalla + generación instantánea de movimientos) |
| [coolcomputery/Loopover-Brute-Force-and-Improvement](https://github.com/coolcomputery/Loopover-Brute-Force-and-Improvement) | Java | Cotas de God's number por block-building: **4×4 GN = 18** (Tomasz Rokicki, coset solver); 5×5 ∈ [22, 42] |
| [coolcomputery/Loopover-NRG-Upper-Bounds](https://github.com/coolcomputery/Loopover-NRG-Upper-Bounds) | Java | Variante *no-regrips* y sus cotas |

## Heurísticas y pattern databases

| Fuente | Qué aporta |
|---|---|
| [alexyuisingwu/sliding-puzzle-database-generator](https://github.com/alexyuisingwu/sliding-puzzle-database-generator) | Java: generador de PDB **aditivas/disjuntas** según Korf & Felner — aplicable al diseño del índice de `PBD` (Tarea 3) |
| [mwong510ca/15Puzzle_OptimalSolver](https://github.com/mwong510ca/15Puzzle_OptimalSolver) | Solver óptimo 15-puzzle con PDB aditivas 7-8 (patrón de partición del espacio) |
| [Walking distance (takaken)](http://www.ic-net.or.jp/home/takaken/nt/slide/solve15.html) | Heurística original de distancia andante para 15-puzzle, base de la teoría en `heuristics.md` |

## Contexto / comunidad

- Hilo [SpeedSolving — God's number de Loopover](https://www.speedsolving.com/threads/loopover-gods-number-upper-bounds-4%C3%974-asymptotics-etc.75180/): cotas superiores, análisis asintóticos, BFS de árboles completos.
- [Codewars kata](https://www.codewars.com/kata/5c1d796370fee68b1e000611): discusiones de soluciones (167 comentarios).

## Ideas transferibles a este proyecto

1. **Documentar la PBD como `heuristics.md`** (torchlight): tamaño de tabla,
   método de indexación, valores medios de poda por distancia → inspira
   [`specs/spec-pbd.md`](specs/spec-pbd.md).
2. **Cotas de optimalidad (GN = 18 en 4×4)** como referencia de máxima longitud
   de solución para pruebas.
3. **Métrica de movimiento unitario** (una fila o columna = 1 movimiento) ya
   coincidente con nuestro coste `1.0f`; cuidado con notaciones ajenas que cuentan
   desplazamientos de N casillas (p. ej. SiGN `xUy` de torchlight) como uno solo.
4. **PDBs disjuntas aditivas** (pares/impares) ≈ nuestra composición
   `PBD_8_SUMA`, con la misma salvedad de admisibilidad.
