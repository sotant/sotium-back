# Changelog

Todos los cambios de este proyecto se documentarán en este archivo siguiendo el formato de [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
Y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

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
