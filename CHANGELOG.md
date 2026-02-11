# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y este proyecto adhiere a [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added
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
