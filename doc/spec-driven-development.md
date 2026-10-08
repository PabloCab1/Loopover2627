# Spec-Driven Development en este proyecto

## Qué es

Desarrollo guiado por especificación: cada unidad de código se escribe primero
como una **especificación verificable** (contrato + criterios de aceptación) y
solo después se implementa. El código no está "hecho" hasta que la spec y los
criterios se cumplen.

```
 ┌─────────────┐   ┌──────────────────┐   ┌────────────┐   ┌──────────────┐
 │ 1. Spec     │ → │ 2. Criterios     │ → │ 3. Tests / │ → │ 4. Código     │
 │ (requisitos)│   │    de aceptación │   │    casos   │   │    (Tarea N)  │
 └─────────────┘   └──────────────────┘   └────────────┘   └──────┬───────┘
        ↑                                                        │
        └────────────── 5. Verificación contra la spec ───────────┘
```

## Flujo por tarea

### 1. Escribir / revisar la spec

- Partir de la plantilla: [`plantilla-spec.md`](plantilla-spec.md).
- Spec ya escrita para cada tarea en [`specs/`](specs/).
- Requisitos extraídos de los Javadoc y TODO del código fuente: la spec
  **formaliza** lo que el esqueleto ya promete, no lo contradice. Si discrepan,
  se resuelve primero el conflicto en la spec.

### 2. Criterios de aceptación

Cada requisito termina en criterios concretos, por ejemplo:

> **CA-1.4**: `new Estado("000102...1415").esResuelto()` devuelve `true`.
> **CA-1.9**: aplicar `00+` a cualquier estado desplaza la fila 0 una casilla
> a la derecha con retorno circular.

Reglas:

- Numerados (`CA-N-M`) para poder referenciarlos en tests y commits.
- Incluyen **casos límite y errores** (longitudes inválidas, fichas duplicadas, acciones fuera de rango).
- Si un criterio no es ejecutable/manualmente comprobable, se reescribe.

### 3. Tests / casos de verificación

- Mínimo: un caso feliz + un caso límite por criterio de error.
- Verificación manual equivalente con la CLI (`verify`, `solve`) cuando aún no
  exista suite de tests:
  ```bash
  java -jar target/loopover.jar verify -s "00010203040506070809101112131415"
  ```

### 4. Implementar

- Solo el código necesario para cumplir los criterios de la spec actual.
- Los Javadoc existentes son la referencia de API; no se cambia una firma sin
  actualizar antes la spec.

### 5. Verificar y cerrar

Checklist de "hecho" para una tarea:

- [ ] Todos los criterios de aceptación de su spec se cumplen.
- [ ] `mvn -q package` compila sin errores.
- [ ] Pruebas manuales/CLI pasan.
- [ ] La spec se marca ✅ y su fila en [doc/README.md](README.md) se actualiza.
- [ ] El commit referencia la spec (`spec-estado: CA-1.4 ...`).

## Reglas de cambio

| Situación | Acción |
|---|---|
| El requisito cambia | Editar spec → commit de spec → luego commit de código |
| Bug (el código incumple la spec) | Fix de código citando el CA violado; la spec no se toca |
| Descubrimiento durante la implementación | Preguntar: ¿es requisito nuevo? → spec; ¿es detalle de diseño? → código |
| Duda sobre semántica | Manda la spec; los Javadoc del esqueleto son evidencia secundaria |

## Relación spec ↔ código

- `specs/spec-estado.md` ↔ `src/Estado.java`
- `specs/spec-busqueda.md` ↔ `src/Busqueda.java`, `src/Nodo.java`, `src/Frontera.java`, `src/Visitados.java`
- `specs/spec-pbd.md` ↔ `src/PBD.java`, `src/Heuristicas.java`, `src/HeuristicasPBD.java`

Cada sección de spec indica el fichero y, cuando es útil, la línea/rango del
Javadoc original de la que se deriva.
