# Implementación del módulo onboarding para registro de academias

## Por qué

Se requiere un punto de entrada API para registrar academias usando un principal autenticado y coordinando capacidades de `identity` y `academy` sin romper límites modulares.

## Qué se modificó

- Se implementó el caso de uso `RegisterAcademyUseCase` en `onboarding`.
- Se añadió la orquestación transaccional en `RegisterAcademyService` con secuencia:
  1. asegurar usuario de identidad por `sub/email` del JWT,
  2. crear academia,
  3. asignar membership `OWNER`.
- Se expuso `POST /api/onboarding/academies` con DTOs validados y extracción de claims (`sub`, `email`) desde JWT.
- Se incorporó manejo de errores con `ProblemDetail` y catálogo mínimo (`VALIDATION_ERROR`, `UNAUTHORIZED`, `FORBIDDEN`, `CONFLICT`, `INTERNAL_ERROR`).
- Se agregaron tests unitarios de orquestación y tests de integración E2E para éxito y rollback “todo o nada”.

## Impacto relevante

- Arquitectura: onboarding se mantiene como orquestador (no dueño de dominio de `academy`/`identity`).
- API: nuevo endpoint autenticado para onboarding sin requerir payload de identidad sensible.
- Consistencia: rollback integral cuando falla la asignación de membership en la misma transacción.
