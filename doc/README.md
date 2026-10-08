# Documentación — Loopover2627

Toda la documentación del proyecto vive aquí, en formato Markdown, siguiendo
**spec-driven development**: la especificación es el contrato que precede al código.

## Mapa de la documentación

| Archivo | Propósito |
|---|---|
| [spec-driven-development.md](spec-driven-development.md) | Flujo de trabajo: spec → criterios de aceptación → test → código |
| [plantilla-spec.md](plantilla-spec.md) | Plantilla reutilizable para escribir nuevas specs |
| [specs/spec-estado.md](specs/spec-estado.md) | **Tarea 1** — `Estado`: bitboard, acciones, sucesores |
| [specs/spec-busqueda.md](specs/spec-busqueda.md) | **Tarea 2** — `Busqueda`, `Nodo`, `Frontera`, `Visitados` |
| [specs/spec-pbd.md](specs/spec-pbd.md) | **Tarea 3** — `PBD`, `Heuristicas`, `HeuristicasPBD` |
| [referencias.md](referencias.md) | Repositorios y fuentes externas de consulta |

## Estado de las specs

| Spec | Tarea | Estado | Código |
|---|---|---|---|
| `spec-estado` | Tarea 1 | ✅ Aprobada | ✅ Implementado y verificado (`src/Estado.java`) |
| `spec-busqueda` | Tarea 2 | 📝 Borrador | ⬜ TODO en `Busqueda`, `Nodo`, `Frontera`, `Visitados` |
| `spec-pbd` | Tarea 3 | 📝 Borrador | ⬜ TODO en `PBD`, `Heuristicas`, `HeuristicasPBD` |

Leyenda: 📝 escrita · ✅ aprobada (spec cerrada) · ⬜ sin implementar · ✅ implementado y verificado

## Convenciones

1. **Una spec por unidad de trabajo.** Si una tarea crece, se divide en specs hijas.
2. **La spec se actualiza antes que el código.** Cambio de requisitos = cambio de spec + commit propio.
3. **Cada criterio de aceptación es verificable.** Si no se puede comprobar, no es un criterio.
4. **Los Javadoc del código citan la spec** y viceversa (nombre de archivo + sección).
5. Los `.md` nuevos se colocan en `doc/` (o `doc/specs/`), nunca en la raíz salvo el `README.md`.
