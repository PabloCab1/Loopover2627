# Spec: <nombre de la unidad>

> **Tarea:** N · **Archivos:** `src/Foo.java`, `src/Bar.java`
> **Estado:** 📝 Borrador | ✅ Aprobada | 🔧 En revisión
> **Última actualización:** YYYY-MM-DD

## 1. Contexto y objetivo

<Qué resuelve esta unidad dentro del proyecto y por qué existe. 2-4 frases.>

## 2. Alcance

**Incluido:**
- ...

**No incluido (explícitamente):**
- ...

## 3. Contrato de API

Firmas esperadas con su semántica. Derivadas de los Javadoc del esqueleto.

| Método | Semántica | Errores |
|---|---|---|
| `Tipo metodo(args)` | ... | `IllegalArgumentException` si ... |

```java
// Firma exacta (copiar del esqueleto si existe)
public Tipo metodo(Args args);
```

## 4. Requisitos funcionales

- **RF-1**: ...
- **RF-2**: ...
- **RF-3**: ...

## 5. Requisitos no funcionales

- **RNF-1 (rendimiento):** ...
- **RNF-2 (memoria):** ...
- **RNF-3 (inmutabilidad/estado):** ...

## 6. Casos límite y errores

| Caso | Entrada | Resultado esperado |
|---|---|---|
| Longitud inválida | `...` | `IllegalArgumentException("...")` |
| Valor en borde | `...` | aceptado |
| Duplicado | `...` | rechazado |

## 7. Criterios de aceptación

- **CA-N-1**: ...
- **CA-N-2**: ...
- **CA-N-3**: ante entrada inválida, lanza `IllegalArgumentException` con mensaje que incluya el valor recibido.

## 8. Estrategia de verificación

- Tests: ...
- Manual CLI: `java -jar target/loopover.jar ...`

## 9. Decisiones y supuestos

- Decisión: ... (motivo)
- Supuesto: ... (validar con: ...)

## 10. Referencias

- Especificaciones relacionadas: [`spec-...`](specs/spec-...)
- Externas: [`referencias.md`](referencias.md)
