# AGENTS.md (por módulo)

> Reglas obligatorias para agentes de IA que trabajen **dentro de este módulo**.
> Este archivo complementa y **no puede contradecir** el `AGENTS.md` raíz.

## 1. Rol del módulo

Este módulo es un **Bounded Context** independiente dentro del monolito modular.

Debe tratarse como un **microservicio en potencia**:
- Autocontenido
- Con límites claros
- Sin dependencias ilegales

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

## 3. Reglas de dependencias (CRÍTICAS)

- `domain` no depende de frameworks.
- `application` depende solo de `domain`.
- `infrastructure` implementa puertos.
- `interfaces` son adaptadores de entrada.

Prohibido:
- Importar infraestructura de otro módulo.
- Acceder a repositorios de otro módulo.
- Compartir entidades JPA.

## 4. Regla final

Si una decisión rompe el aislamiento del módulo:

> **DETENTE Y PREGUNTA.**
