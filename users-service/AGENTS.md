# AGENTS.md (por módulo)

> Reglas obligatorias para agentes de IA que trabajen **dentro de este módulo**.
> Este archivo complementa y **no puede contradecir** el `AGENTS.md` raíz.

---

## 1. Rol del módulo

Este módulo es un **Bounded Context** independiente dentro del monolito modular.

Debe tratarse como un **microservicio en potencia**:
- Autocontenido
- Con límites claros
- Sin dependencias ilegales

---

## 2. Estructura obligatoria

```text
<module-name>/
└── com.sotium.<module>
    ├── domain
    │   ├── model
    │   ├── valueobject
    │   ├── repository
    │   └── exception
    │
    ├── application
    │   ├── port
    │   │   ├── in
    │   │   └── out
    │   └── usecase
    │
    ├── infrastructure
    │   ├── persistence
    │   ├── messaging
    │   ├── external
    │   └── config
    │
    └── interfaces
```

---

## 3. Reglas de dependencias (CRÍTICAS)

- `domain` no depende de frameworks.
- `application` depende solo de `domain`.
- `infrastructure` implementa puertos.
- `interfaces` son adaptadores de entrada.

Prohibido:
- Importar infraestructura de otro módulo.
- Acceder a repositorios de otro módulo.
- Compartir entidades JPA.

---

## 4. Dominio

Contiene:
- Entidades
- Agregados
- Value Objects
- Invariantes
- Excepciones
- Puertos (repositorios)

Prohibido:
- Spring
- JPA
- HTTP
- DTOs REST

---

## 5. Application

- Implementa casos de uso.
- Orquesta dominio.
- Define puertos.
- No contiene lógica de infraestructura.

---

## 6. Interfaces (REST)

- Controllers delgados.
- Validación con `@Valid`.
- No exponer entidades JPA.
- Uso obligatorio de mappers.

---

## 7. Persistencia

- Tablas exclusivas del módulo.
- Sin claves foráneas entre módulos.
- Transacciones en application.

---

## 8. Testing

- Domain y Application sin Spring.
- Infrastructure con tests de integración.

---

## 9. Estándares técnicos

### Uso de Java 21

- El uso de `var` está permitido **solo cuando el tipo es evidente sin IDE**.
  - Si el método o constructor no expresa claramente el tipo de retorno → **usar tipo explícito**.
  - Los nombres de variables deben ser descriptivos para compensar la inferencia.

- Priorizar **inmutabilidad**:
  - DTOs, comandos y eventos **DEBEN ser `record`**.
  - Evitar objetos mutables innecesarios.
- No testear getters/setters generados por `record`.

### Modelado y control de flujo

- Priorizar **Pattern Matching** y `switch` con records para lógica ramificada.
- Evitar `null` en colecciones; preferir colecciones vacías o `Optional` como retorno.
- Usar **Sequenced Collections** (`getFirst()`, `getLast()`, `reversed()`) cuando el orden sea relevante.

### Spring y dependencias

- Inyección de dependencias **exclusivamente por constructor**.
- Prohibido `@Autowired` en campos.
- Prohibido `System.out` / `System.err`; usar logging con SLF4J.

### HTTP y errores

- Usar `RestClient` como cliente HTTP estándar.
- Seguir **RFC 9457 – Problem Details for HTTP APIs** para errores REST.
  - Preferir `ProblemDetail` o `ErrorResponseException`.

### Testing del módulo

- Tests unitarios centrados en lógica de dominio y aplicación.
- Tests de integración usando Testcontainers cuando haya infraestructura real.
- Nombrar tests de forma descriptiva (`@DisplayName` o `given_When_Then`).
- Prohibido usar H2 u otras bases en memoria para simular producción.
- Prohibido `Thread.sleep()` en tests para esperar procesos asíncronos (usar Awaitility u otro mecanismo explícito).

Estas reglas aplican **dentro del módulo** y no deben romper su aislamiento.

---

## 10. Regla final

Si una decisión rompe el aislamiento del módulo:

> **DETENTE Y PREGUNTA.**
