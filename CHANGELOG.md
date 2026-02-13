# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Se agregaron tests de seguridad para converter JWT, `SecurityContextFacade`, filtros de tenant (`resolution`/`enforcement`), `TenantSelection` y reglas de acceso público/protegido en `ResourceServerConfig`.
- Se añadieron tests web/slice con `@WebMvcTest` para `MeController`, `PublicIdentityController` y `GlobalExceptionHandler`, cubriendo respuestas 200/403/401/400 y payloads JSON esperados.
- Se añadieron pruebas unitarias para `Role` y `AuthenticatedUser` en `shared-security`, cubriendo tenant scope, authorities, normalización de realm roles e inmutabilidad de colecciones.
- Se amplió la cobertura de `ResolveTenantContextService` y se añadieron pruebas para `TenantAccessPortAdapter` en `identity`, validando happy paths y traducción de excepciones de acceso a tenant.
- Se añadió diagrama de flujo happy path de `GET /api/identity/me` y trazado narrado en `docs/IDENTITY_SHARED-SECURITY_ARQUITECTURA_HEXAGONAL.md`.
- Documentación técnica detallada de arquitectura hexagonal y seguridad para módulos `identity` y `shared-security` en `docs/IDENTITY_SHARED-SECURITY_ARQUITECTURA_HEXAGONAL.md`.
- Integración base de OAuth2 Resource Server en `shared-security` con validación de issuer, JWKS y audience para Keycloak.
- Conversión de roles realm de Keycloak a authorities de Spring Security filtrando roles técnicos.
- Contexto de tenant por request (`TenantContextHolder`) y filtros de resolución/enforcement.
- Puerto `TenantAccessPort` e implementación en `identity` para validación strict de usuario provisionado y membership activa.
- Endpoints de smoke test (`/api/public/identity/academy-registration` y `/api/identity/me`) para pruebas de autenticación/autorización.
- Guía operativa de integración y escenarios Postman en `docs/keycloak-resource-server-integration.md`.

### Changed
- Refactor de roles de seguridad a dominio (`Role` y `AuthenticatedUser`) eliminando strings hardcoded de roles en filtros de tenant y converter JWT.
- Se incorporó Lombok en `identity` y `shared-security` para reducir boilerplate (constructores, getters de entidades y logging).
- Se simplificaron JavaDocs dejando solo documentación en clases/métodos donde aporta contexto real.
- Se añadieron logs de seguridad/tenant sin exponer datos sensibles para trazabilidad del flujo.
- Refactor de paquetes en `identity` y `shared-security` para alinear la estructura a domain/application/infrastructure/interfaces según arquitectura hexagonal + clean.
- Se añadieron JavaDoc en inglés para clases y métodos nuevos de seguridad, tenant e identity.
- Eliminada la configuración duplicada `bootstrap/src/main/java/com/sotium/bootstrap/config/SecurityConfig.java` para usar solo `ResourceServerConfig`.
- `bootstrap/src/main/resources/application.yml` mantiene `server.port=8080` y datasource sin valores por defecto.
