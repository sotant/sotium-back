# Contratos modulares, ownership de datos y excepción de seguridad para onboarding

## Objetivo

Este documento resume los límites entre módulos `academy`, `identity`, `onboarding` y `shared-security`, incluyendo ownership de tablas, contratos públicos/internos y la excepción de seguridad acotada para el onboarding inicial.

## Roles y límites por módulo

### `academy`
- **Rol**: dueño del dominio de academias y su configuración operativa.
- **No hace**: no provisiona usuarios de identidad ni memberships.
- **Contrato público usado por otros módulos**:
  - `AcademyRegistrationPort#createAcademy(CreateAcademyCommand)`.

### `identity`
- **Rol**: dueño de identidad de usuario y memberships por academia.
- **No hace**: no crea academias.
- **Contratos públicos usados por otros módulos**:
  - `EnsureIdentityUserExistsFromTokenUseCase#ensure(...)`.
  - `AssignOwnerMembershipUseCase#assign(...)`.
  - `ResolveTenantContextUseCase` para resolución de tenancy consumida por `shared-security`.

### `onboarding`
- **Rol**: orquestador de alta de academia en una transacción de aplicación.
- **No es dueño**: de tablas de `academy` ni `identity`.
- **Contrato API público**:
  - `POST /api/onboarding/academies`.
- **Contratos internos consumidos**:
  - `AcademyRegistrationPort` (academy).
  - `EnsureIdentityUserExistsFromTokenUseCase` (identity).
  - `AssignOwnerMembershipUseCase` (identity).

### `shared-security`
- **Rol**: seguridad transversal (JWT Resource Server, filtros de tenant, enforcement).
- **No hace**: no contiene lógica de negocio de onboarding.
- **Contrato interno clave**:
  - `TenantAccessPort` para consultar acceso tenant vía `identity` sin acoplar JPA.

## Ownership de tablas

- `identity`:
  - `identity_users`
  - `academy_memberships`
- `academy`:
  - `academies`
  - `academy_settings`
- `onboarding`:
  - No posee tablas en esta iteración (solo orquesta).
- `shared-security`:
  - No posee tablas de negocio.

## Contratos de API y puertos

### API pública (HTTP)
- `POST /api/onboarding/academies`
  - Auth JWT requerida.
  - Body con datos de academia (`name`, `email`, `phone`, `timezone`).
  - `sub` y `email` del owner se extraen del token, no del body.

### Contratos internos (puertos)
- `onboarding -> identity`
  - asegurar usuario por token.
  - asignar OWNER membership.
- `onboarding -> academy`
  - crear academia y settings.
- `shared-security -> identity`
  - resolver tenant y validar pertenencia.

## Criterio de idempotencia

En esta línea base:

- **Identity sí aplica idempotencia**:
  - `EnsureIdentityUserExistsFromToken`: evita duplicados por `keycloak_sub` y sincroniza email bajo reglas de conflicto.
  - `AssignOwnerMembership`: trata reintentos/colisiones de unique como estado ya existente.

- **Onboarding no implementa idempotencia propia (por ahora)**:
  - no hay `Idempotency-Key` ni tabla de tracking.
  - se usa consistencia transaccional para evitar estados parciales.

## Excepción de seguridad: onboarding inicial sin tenant

### Qué se permite

Se habilita únicamente el bypass de tenant para:
- `POST /api/onboarding/academies`

### Por qué es seguro

- No desactiva autenticación: el endpoint sigue requiriendo JWT válido.
- No desactiva enforcement global: rutas tenant-scoped siguen requiriendo tenant resuelto.
- El bypass es **específico por método + path exacto**, minimizando superficie de riesgo.

### Resultado esperado

- Sin token en onboarding: `401`.
- Con token en onboarding y sin tenant: permitido.
- Con token y sin tenant en rutas tenant-scoped: denegado (`403` según política actual).
