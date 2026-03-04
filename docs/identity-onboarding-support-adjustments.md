# Ajustes en `identity` para soporte de onboarding de academias

## Por qué se realizó este cambio

El flujo de registro de academia necesita capacidades idempotentes en el contexto de identidad para evitar errores en reintentos y mantener consistencia entre autenticación (JWT) y autorización tenant.

Además, `shared-security` requiere consultar memberships por contrato estable, sin acoplarse a JPA ni a detalles internos de persistencia.

## Auditoría breve previa (reutilizado vs nuevo)

### Piezas reutilizadas

- Modelo de dominio existente:
  - `IdentityUser`
  - `AcademyMembership`
  - enums de estado/rol
- Caso de uso ya existente:
  - `ResolveTenantContextService` / `ResolveTenantContextUseCase`
- Integración ya existente con `shared-security`:
  - `TenantAccessPortAdapter` implementando `TenantAccessPort`

### Piezas nuevas

- Caso de uso idempotente `EnsureIdentityUserExistsFromTokenUseCase`.
- Caso de uso idempotente `AssignOwnerMembershipUseCase`.
- Puerto de consulta de acceso tenant `ResolveIdentityTenantAccessUseCase`.
- Extensiones de puertos de repositorio de `identity` para `save` y búsquedas necesarias.
- Manejo explícito de conflictos:
  - `IdentityUserEmailConflictException` para conflicto de email único.
  - `MembershipAlreadyExistsException` para carrera por unique de membership.

## Qué se modificó

- Se implementó `EnsureIdentityUserExistsFromTokenService`:
  - crea `identity_user` cuando `keycloak_sub` no existe.
  - actualiza email cuando cambia y no colisiona con otro usuario.
  - mantiene idempotencia para llamadas repetidas con mismo `sub`.
- Se implementó `AssignOwnerMembershipService`:
  - crea membership `OWNER` + `ACTIVE` cuando no existe.
  - devuelve estado `alreadyExisted` cuando ya existía.
  - trata colisión por unique `(academy_id, user_id)` como éxito idempotente.
- Se añadió `ResolveIdentityTenantAccessService` para:
  - resolver academias accesibles por `sub` con membership activo.
  - validar acceso booleano `sub -> academyId`.
- Se ajustó persistencia JPA de `identity`:
  - soporte para `created_at` / `updated_at` en `identity_users`.
  - soporte para `created_at` y unique compuesto en `academy_memberships`.
  - nuevos métodos en repos Spring Data para búsquedas por email y por par `(academyId, userId)`.

## Constraints de base de datos

Se verificó contra el esquema base que ya existen:

- `identity_users.keycloak_sub UNIQUE`
- `academy_memberships UNIQUE(academy_id, user_id)`

Por lo tanto **no se añadió migración nueva**, evitando duplicidad.

## Impacto relevante

### Arquitectura

- Se mantiene arquitectura hexagonal:
  - Application depende de puertos.
  - Infraestructura traduce de/para JPA.
- `shared-security` sigue consumiendo contratos de `identity` sin dependencia a entidades JPA.

### Seguridad / operación

- El sistema tolera reintentos en provisioning de usuario y asignación owner sin duplicar registros.
- Se mantiene trazabilidad temporal en `identity_users` mediante actualización de `updated_at` al guardar.
