# Ajustes en `identity` para onboarding idempotente de academias

## Por qué

El flujo de onboarding necesita tolerancia a reintentos y concurrencia cuando el principal autenticado llega desde JWT de Keycloak.
Sin capacidades idempotentes en `identity`, se pueden producir duplicados lógicos o fallos por colisiones de unicidad en escenarios normales de retry.

## Qué se modificó

- Se agregó el caso de uso `EnsureIdentityUserExistsFromTokenUseCase` para garantizar un `identity_user` por `keycloak_sub`.
  - Si el usuario no existe, se crea con `status=ACTIVE`.
  - Si existe, solo se permite sincronizar `email` cuando cambió.
  - Si el email ya pertenece a otro usuario, se lanza `IdentityUserEmailConflictException`.
- Se agregó el caso de uso `AssignOwnerMembershipUseCase` para asignar `OWNER` en `academy_memberships` de forma idempotente.
  - Si la membresía existe para `(academy_id, user_id)`, responde como `alreadyExisted=true`.
  - Si no existe, crea con `role=OWNER` y `status=ACTIVE`.
  - Si hay carrera y colisión de `UNIQUE`, se interpreta como estado ya existente.
- Se extendió el puerto de tenant existente (`ResolveTenantContextUseCase`) con capacidades de consulta estables para `shared-security`:
  - resolver academias accesibles por `sub` (`resolveAccessibleAcademyIds`)
  - validar acceso `sub -> academyId` (`hasAccessToAcademy`)

## Reutilización vs. creación nueva

- **Reutilizado**:
  - Dominio `IdentityUser`, `AcademyMembership`, enums de rol/estado.
  - `ResolveTenantContextService` + `TenantAccessPortAdapter` para integración con `shared-security`.
- **Nuevo/extendido**:
  - Nuevos casos de uso idempotentes (3.1 y 3.2).
  - Métodos de persistencia para búsqueda por email, upsert aplicativo y lookup por `(academyId, userId)`.

## Impacto relevante

- Arquitectura: se mantiene hexagonal; `shared-security` sigue consumiendo puertos de `identity` sin acoplarse a JPA.
- Base de datos: no se agregan tablas ni FKs; se confirmó que los `UNIQUE` requeridos ya están definidos en el esquema base (`identity_users.keycloak_sub` y `academy_memberships(academy_id, user_id)`).
- API: no se agregan endpoints; el cambio es interno de aplicación/integración.
