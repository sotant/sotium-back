# Integración Keycloak Resource Server (identity + shared-security)

## Resumen
Esta implementación deja una base técnica para:

1. Validación de JWT de Keycloak (issuer + JWKS + audience `bff-web`).
2. Extracción de identidad (`sub`, `email`).
3. Mapeo de roles de realm a authorities de Spring (`ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_TEACHER`, `ROLE_STUDENT`).
4. Resolución de tenant por request usando `academy_id` desde header/cookie + validación en `identity`.
5. Estrategia strict: JWT válido pero usuario inexistente/no activo en BD => `403 Forbidden`.

## Configuración requerida
En `bootstrap/src/main/resources/application.yml`:

- `spring.security.oauth2.resourceserver.jwt.issuer-uri`
- `security.oauth2.audience`

Valores por defecto local:

- issuer: `http://localhost:8080/realms/sotium-staging`
- audience: `bff-web`

## Flujo de request autenticada
1. Spring Security valida firma/issuer/exp/JWKS + audience.
2. `KeycloakRealmRoleJwtAuthenticationConverter` mapea roles realm.
3. `TenantResolutionFilter` toma `AuthenticatedUser` + selección de academia (`X-Academy-Id` o cookie `academy_id`).
4. `TenantAccessPort` (implementado en `identity.infrastructure.security`) valida usuario y membership activa.
5. Si pasa, se guarda `TenantContext` en `ThreadLocal` por request.
6. `TenantEnforcementFilter` exige tenant para roles tenant-scoped.

## Endpoints de validación rápida
- Público: `GET /api/public/identity/academy-registration`
- Protegido tenant: `GET /api/identity/me`

## Colección Postman (escenarios)
Configurar variables:

- `{{apiUrl}}` = `http://localhost:8080`
- `{{accessToken}}` = token JWT Keycloak
- `{{academyId}}` = academia activa del usuario

### 1) Token válido + usuario existente + membership activa => 200
```http
GET {{apiUrl}}/api/identity/me
Authorization: Bearer {{accessToken}}
X-Academy-Id: {{academyId}}
```

### 2) Sin token => 401
```http
GET {{apiUrl}}/api/identity/me
```

### 3) Token válido + usuario no existe en BD => 403
```http
GET {{apiUrl}}/api/identity/me
Authorization: Bearer {{accessTokenDeUsuarioNoProvisionado}}
X-Academy-Id: {{academyId}}
```

### 4) Rol insuficiente => 403
Consumir endpoint protegido con `@PreAuthorize` de rol superior desde `identity` cuando exista caso de uso administrativo.

### 5) Token válido + membership no activa => 403
Usar usuario con membership `INVITED` o `DISABLED`.

### 6) Token válido + academia activa inválida/no perteneciente => 403
```http
GET {{apiUrl}}/api/identity/me
Authorization: Bearer {{accessToken}}
X-Academy-Id: {{academyIdNoAsociada}}
```

## Notas de arquitectura
- `shared-security` no toca repositorios JPA ni reglas de negocio de membership.
- `shared-security` se organiza en `domain/application/infrastructure/interfaces`.
- `identity` se organiza en `domain/application/infrastructure/interfaces` y expone `TenantAccessPortAdapter` en infraestructura de seguridad.
- No se usa `academy_id` desde claims del token.
