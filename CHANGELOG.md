# Changelog

Todos los cambios de este proyecto se documentarán en este archivo siguiendo el formato de [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
Y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Endpoint público `GET /api/public/identity/users` para listar usuarios de una academia, devolviendo todos los campos de `user_profiles` más `email` de `identity_users`.
- Caso de uso `ListAcademyUsersPublicUseCase` y servicio de aplicación para resolver membresías por academia y consolidar perfil + email por usuario.
- Documentación técnica del cambio en `docs/identity-public-list-academy-users.md`.
- Endpoint público `POST /api/public/identity/register-user` en `identity` para alta de usuario, creación de perfil (`user_profiles`) y membresía activa con rol `USER` en `academy_memberships`.
- Caso de uso transaccional `RegisterPublicUserUseCase` con generación de `keycloak_sub` aleatorio y fallback de datos mock para campos opcionales de perfil.
- Persistencia de perfiles de usuario en `identity` (`JpaUserProfileEntity`, repositorio Spring Data y adaptador), con cobertura de tests unitarios del caso de uso.
- Documentación técnica del cambio en `docs/identity-public-register-user.md`.
- Casos de uso idempotentes en `identity` para onboarding: `EnsureIdentityUserExistsFromTokenUseCase` y `AssignOwnerMembershipUseCase`, con cobertura de tests unitarios para creación, actualización, conflictos y reintentos concurrentes.
- Extensión del contrato de tenant en `identity` para exponer academias accesibles por `sub` y validación booleana `sub -> academyId`, manteniendo la integración por puertos con `shared-security`.
- Implementación funcional del módulo `onboarding` con caso de uso `RegisterAcademyUseCase`, endpoint `POST /api/onboarding/academies`, DTOs validados y manejo de errores con `ProblemDetail`.
- Tests del módulo `onboarding` (unitarios y web) y tests de integración E2E en `bootstrap` para validar flujo completo y rollback transaccional.
- Documentación técnica en `docs/identity-onboarding-idempotency.md` y `docs/onboarding-module-registration-orchestration.md`, además de `onboarding/README.md` para alcance y límites del módulo.
- Ajuste en `shared-security` para permitir únicamente `POST /api/onboarding/academies` sin tenant resuelto, manteniendo autenticación JWT obligatoria y enforcement tenant para rutas tenant-scoped.
- Tests de no regresión de seguridad para onboarding y rutas tenant-scoped sin tenant.
- Documentación `docs/modular-contracts-and-security-exception.md` con límites de módulos, ownership de tablas y criterios de seguridad/idempotencia.
- Endpoint público `POST /api/public/identity/purge-by-sub` en `identity` para borrar por `sub` la membresía, la academia asociada y la identidad de base de datos.
- Caso de uso transaccional `DeleteIdentityBySubUseCase` en `identity`, con cobertura de tests unitarios para borrado efectivo y no-op cuando el `sub` no existe.
- Documentación técnica del cambio en `docs/identity-public-purge-by-sub.md`.

### Changed
- Incremento de versión del proyecto a `0.9.0-SNAPSHOT` siguiendo SemVer con bump MINOR por incorporación del endpoint público de listado de usuarios por academia en `identity`.
- Incremento de versión del proyecto a `0.8.0-SNAPSHOT` siguiendo SemVer con bump MINOR por incorporación del endpoint público de alta de usuarios en `identity`.
- Adaptadores de persistencia de `identity` ampliados para soportar operaciones idempotentes (`findByEmail`, `save`, `findByAcademyIdAndUserId`) sin acoplar la capa de aplicación a JPA.
- Incremento de versión del proyecto a `0.7.0-SNAPSHOT` siguiendo SemVer con bump MINOR por incorporación funcional del módulo `onboarding` y el endpoint de purga pública en `identity`.
- Contratos y adaptadores de persistencia de `identity` y `academy` ampliados para soportar operaciones de borrado requeridas por la purga pública.

## [0.1.0] - 2026-03-03

### Added
- Estructura de **monolito modular Maven** con módulos `shared`, `shared-security`, `identity` y `bootstrap`, bajo Java 21 y Spring Boot 4.0.2.
- Módulo `bootstrap` como punto de entrada de la aplicación (`SotiumApplication`) y configuración transversal inicial para transacciones (`TransactionConfig`) y mensajería (`MessagingConfig`).
- Configuración base de aplicación en `bootstrap/src/main/resources/application.yml` con puerto HTTP y propiedades para datasource y seguridad OAuth2 Resource Server.
- Módulo `shared` con excepciones de dominio reutilizables (`NotFoundException`, `ConflictException`) y manejador global de excepciones web (`GlobalExceptionHandler`).
- Módulo `shared-security` implementado con enfoque hexagonal:
    - Modelos de dominio de seguridad (`Role`, `AuthenticatedUser`, `TenantContext`).
    - Puerto de salida `TenantAccessPort` para desacoplar validación de pertenencia/tenancy.
    - Configuración de Resource Server (`ResourceServerConfig`) con soporte JWT.
    - Conversor de claims/roles de Keycloak (`KeycloakRealmRoleJwtAuthenticationConverter`) y utilidades de contexto (`SecurityContextFacade`, `JwtClaimsExtractor`).
    - Filtros web para resolución y enforcement de tenant (`TenantResolutionFilter`, `TenantEnforcementFilter`) y utilidades de selección/contexto (`TenantSelection`, `TenantContextHolder`).
- Módulo `identity` implementado con enfoque hexagonal:
    - Dominio de identidad y membresía (`IdentityUser`, `AcademyMembership`) con enums de estado/rol (`IdentityUserStatus`, `MembershipStatus`, `MembershipRole`).
    - Caso de uso `ResolveTenantContextService` y puerto de entrada `ResolveTenantContextUseCase` para resolver contexto tenant por request.
    - Puertos de salida para persistencia (`IdentityUserRepository`, `MembershipRepository`).
    - Adaptadores de infraestructura para JPA (`IdentityUserRepositoryAdapter`, `MembershipRepositoryAdapter`) con repositorios Spring Data y mapeadores de persistencia.
    - Entidades JPA específicas del módulo (`JpaIdentityUserEntity`, `JpaMembershipEntity`).
    - Adaptador `TenantAccessPortAdapter` para integrar validación de tenant entre `shared-security` e `identity`.
    - Endpoints REST iniciales: endpoint público de smoke test (`/api/public/identity/academy-registration`) y endpoint autenticado de perfil/contexto (`/api/identity/me`) con DTO `MeResponse`.
- Pruebas automatizadas iniciales:
    - Tests unitarios de dominio en `shared-security` (`RoleTest`, `AuthenticatedUserTest`).
    - Tests unitarios/aislados de filtros de tenant y utilidades de seguridad.
    - Tests de configuración de seguridad y JWT en Resource Server.
    - Tests web con `@WebMvcTest` para controladores de `identity` y handlers de excepciones.
    - Tests de aplicación para `ResolveTenantContextService`.
    - Test de arquitectura (`HexagonalArchitectureTest`) para validar reglas de capas y límites.
    - Test de integración extremo a extremo (`IdentitySecurityIntegrationTest`) para el flujo autenticado de identidad + seguridad.
- Documentación funcional/técnica existente:
    - Guía operativa de integración Keycloak Resource Server y escenarios de prueba (`docs/keycloak-resource-server-integration.md`).
    - ADR aceptado sobre uso acotado de anotaciones Spring en capa Application (`docs/ADR_spring_in_application_layer.md`).
    - Script SQL de arquitectura inicial de base de datos (`docs/V1__initial_architecture.sql`) con diseño modular por dominios funcionales.

### Security
- Se estableció autenticación JWT con validación de issuer, JWKS y audience para proteger endpoints privados.
- Se formalizó el modelo de autorización por roles de realm y alcance por tenant para endpoints sensibles.
- Se implementó la estrategia strict de acceso: token válido sin usuario/membership activa en persistencia deriva en denegación de acceso.

### Architecture
- Se consolidó arquitectura hexagonal/clean por módulos, con separación explícita entre dominio, aplicación, infraestructura e interfaces.
- Se incorporó validación automatizada de reglas arquitectónicas para prevenir dependencias ilegales entre capas y bounded contexts.
