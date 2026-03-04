# Onboarding module

## Responsabilidad

El módulo `onboarding` orquesta el registro inicial de academias en una sola transacción de aplicación.
No implementa lógica de dominio propia de `academy` ni `identity`; consume sus capacidades públicas vía puertos.

## Límites

- No usa repositorios ni entidades JPA de otros módulos.
- No crea ownership de tablas fuera de `academy` e `identity`.
- No define reglas de negocio de deduplicación ni idempotencia en esta iteración.

## Puertos consumidos

- `AcademyRegistrationPort` (módulo `academy`)
- `EnsureIdentityUserExistsFromTokenUseCase` (módulo `identity`)
- `AssignOwnerMembershipUseCase` (módulo `identity`)
