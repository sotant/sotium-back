# IDENTITY_SHARED-SECURITY_ARQUITECTURA_HEXAGONAL.md

## Resumen ejecutivo

Este documento analiza **exclusivamente** el código disponible en los módulos `identity` y `shared-security` del repositorio, sin inferencias no respaldadas.

Hallazgos clave:

1. `shared-security` centraliza la integración con **Spring Security OAuth2 Resource Server** (JWT con issuer + audience), la construcción de `Authentication` desde claims de Keycloak, y el pipeline de resolución/enforcement de tenant por request.
2. `identity` implementa la lógica de negocio de resolución de tenant (academy) basada en usuario provisionado y memberships activas, y expone endpoints `me` y endpoint público de probe.
3. La integración entre módulos se hace por **puerto saliente** `TenantAccessPort` (en `shared-security`) implementado por `TenantAccessPortAdapter` (en `identity`), alineado con hexagonal.
4. El concepto de “usuario actual” (`me`) se deriva del `Authentication.details` con tipo `AuthenticatedUser` seteado por el converter JWT.
5. El multi-tenant por fila (`academy_id`) se resuelve por request desde header/cookie + validación en `identity`; si el rol es tenant-scoped y no se logra contexto, se rechaza con `403`.

---

## Alcance verificado y límites

### Módulos analizados
- `identity`
- `shared-security`

### Archivos inspeccionados (indexación previa obligatoria)

#### `identity`
- `src/main/java/com/sotium/identity/application/exception/IdentityAccessDeniedException.java`
- `src/main/java/com/sotium/identity/application/port/in/ResolveTenantContextUseCase.java`
- `src/main/java/com/sotium/identity/application/port/out/IdentityUserRepository.java`
- `src/main/java/com/sotium/identity/application/port/out/MembershipRepository.java`
- `src/main/java/com/sotium/identity/application/usecase/ResolveTenantContextService.java`
- `src/main/java/com/sotium/identity/domain/model/AcademyMembership.java`
- `src/main/java/com/sotium/identity/domain/model/IdentityUser.java`
- `src/main/java/com/sotium/identity/domain/model/IdentityUserStatus.java`
- `src/main/java/com/sotium/identity/domain/model/MembershipRole.java`
- `src/main/java/com/sotium/identity/domain/model/MembershipStatus.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/IdentityUserRepositoryAdapter.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/JpaIdentityUserEntity.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/JpaMembershipEntity.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/MembershipRepositoryAdapter.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/PersistenceMappers.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/SpringDataIdentityUserRepository.java`
- `src/main/java/com/sotium/identity/infrastructure/persistence/SpringDataMembershipRepository.java`
- `src/main/java/com/sotium/identity/infrastructure/security/TenantAccessPortAdapter.java`
- `src/main/java/com/sotium/identity/interfaces/web/MeController.java`
- `src/main/java/com/sotium/identity/interfaces/web/MeResponse.java`
- `src/main/java/com/sotium/identity/interfaces/web/PublicIdentityController.java`
- `src/main/resources/application.yml`
- `src/test/java/com/sotium/identity/application/usecase/ResolveTenantContextServiceTest.java`

#### `shared-security`
- `src/main/java/com/sotium/shared/security/application/port/out/TenantAccessPort.java`
- `src/main/java/com/sotium/shared/security/domain/model/AuthenticatedUser.java`
- `src/main/java/com/sotium/shared/security/domain/model/TenantContext.java`
- `src/main/java/com/sotium/shared/security/infrastructure/security/JwtClaimsExtractor.java`
- `src/main/java/com/sotium/shared/security/infrastructure/security/KeycloakRealmRoleJwtAuthenticationConverter.java`
- `src/main/java/com/sotium/shared/security/infrastructure/security/ResourceServerConfig.java`
- `src/main/java/com/sotium/shared/security/infrastructure/security/SecurityContextFacade.java`
- `src/main/java/com/sotium/shared/security/infrastructure/security/exceptions/ForbiddenException.java`
- `src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantContextHolder.java`
- `src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantEnforcementFilter.java`
- `src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantResolutionFilter.java`
- `src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantSelection.java`
- `src/main/java/com/sotium/shared/security/interfaces/rest/ApiError.java`
- `src/main/java/com/sotium/shared/security/interfaces/rest/GlobalExceptionHandler.java`

### Puntos no verificables con lo disponible
- No hay configuración visible de base de datos/Flyway en estos dos módulos.
- No hay controladores adicionales en `identity` más allá de `me` y el probe público.
- No hay evidencias de eventos de dominio, mensajería o integración async entre módulos.
- No hay `@PreAuthorize`/anotaciones de autorización por método en clases inspeccionadas.
- No hay configuración de CORS, session management ni custom `AuthenticationEntryPoint`/`AccessDeniedHandler` adicional.

---

## Qué problema resuelve cada módulo

### `shared-security`
**Resuelve:**
- Validación JWT para Resource Server (issuer + audience).
- Traducción claims → principal/authorities internos.
- Resolución de contexto tenant por request y enforcement de presencia para roles no-admin.
- Manejo global de errores de seguridad (`401/403/400`) en formato uniforme (`ApiError`).

**No debería tener (según su rol actual):**
- Lógica de negocio de identidad/membresías (delegada por puerto `TenantAccessPort`).
- Persistencia propia de usuarios/membresías.

### `identity`
**Resuelve:**
- Lógica de negocio para resolver `academyId` permitido para un usuario autenticado (`keycloakSub`) usando repositorios de identidad + membership.
- Adaptación de esa lógica a `shared-security` mediante `TenantAccessPortAdapter`.
- Endpoint `/api/identity/me` para exponer identidad actual + tenant resuelto.

**No debería tener (según su rol actual):**
- Validación criptográfica del JWT (eso está en `shared-security`).
- Gestión directa del filter chain de Spring Security.

---

## Mapa de arquitectura hexagonal aplicado

## Capas por módulo

### `identity`
- **Dominio**
  - `domain.model.*` (`IdentityUser`, `AcademyMembership`, enums de estado/rol)
- **Aplicación**
  - `application.port.in.ResolveTenantContextUseCase`
  - `application.port.out.IdentityUserRepository`, `MembershipRepository`
  - `application.usecase.ResolveTenantContextService`
  - `application.exception.IdentityAccessDeniedException`
- **Adaptadores de salida (infra/outbound)**
  - `infrastructure.persistence.*` (JPA + adapters repos)
  - `infrastructure.security.TenantAccessPortAdapter` (implementa puerto de otro módulo)
- **Adaptadores de entrada (interfaces/entrypoints)**
  - `interfaces.web.MeController`, `PublicIdentityController`

### `shared-security`
- **Dominio compartido de seguridad**
  - `domain.model.AuthenticatedUser`, `TenantContext`
- **Aplicación / contrato**
  - `application.port.out.TenantAccessPort`
- **Infraestructura seguridad y web filters**
  - `infrastructure.security.*`
  - `infrastructure.web.filter.*`
- **Entrypoint REST transversal**
  - `interfaces.rest.GlobalExceptionHandler`, `ApiError`

## Puertos y adaptadores (explícitos)

### Puertos
1. `com.sotium.shared.security.application.port.out.TenantAccessPort`
   - Contrato: resolver academy activa en base a usuario autenticado + selección opcional.
2. `com.sotium.identity.application.port.in.ResolveTenantContextUseCase`
   - Contrato de entrada del caso de uso de identidad.
3. `com.sotium.identity.application.port.out.IdentityUserRepository`
4. `com.sotium.identity.application.port.out.MembershipRepository`

### Adaptadores
1. `com.sotium.identity.infrastructure.security.TenantAccessPortAdapter`
   - Implementa `TenantAccessPort` delegando al caso de uso de identity.
2. `IdentityUserRepositoryAdapter`
   - Implementa `IdentityUserRepository` con Spring Data JPA.
3. `MembershipRepositoryAdapter`
   - Implementa `MembershipRepository` con Spring Data JPA.
4. `MeController` / `PublicIdentityController`
   - Adaptadores de entrada HTTP para identity.
5. `TenantResolutionFilter`, `TenantEnforcementFilter`
   - Adaptadores de entrada cross-cutting (HTTP filter chain).

---

## Diagrama de dependencias permitidas (mermaid)

```mermaid
flowchart LR
    subgraph SHARED_SECURITY
      SSCFG[ResourceServerConfig]
      CONV[KeycloakRealmRoleJwtAuthenticationConverter]
      SCF[SecurityContextFacade]
      TRF[TenantResolutionFilter]
      TEF[TenantEnforcementFilter]
      TAP[TenantAccessPort (port)]
      TCH[TenantContextHolder]
      GH[GlobalExceptionHandler]
    end

    subgraph IDENTITY
      RCU[ResolveTenantContextUseCase (port-in)]
      RCS[ResolveTenantContextService]
      IUR[IdentityUserRepository (port-out)]
      MR[MembershipRepository (port-out)]
      IURA[IdentityUserRepositoryAdapter]
      MRA[MembershipRepositoryAdapter]
      TAPA[TenantAccessPortAdapter]
      MEC[MeController]
    end

    TRF --> TAP
    TAPA --> RCU
    RCS --> IUR
    RCS --> MR
    IURA --> IUR
    MRA --> MR
    MEC --> SCF
    MEC --> TCH
    TEF --> SCF
    TEF --> TCH
    SSCFG --> TRF
    SSCFG --> TEF
    SSCFG --> CONV
    GH --> TEF
    GH --> SCF
```

---

## Flujos principales

### 1) Validación JWT y construcción del principal
1. `ResourceServerConfig.resourceServerFilterChain(...)` configura `oauth2ResourceServer().jwt(...)` usando `KeycloakRealmRoleJwtAuthenticationConverter`.
2. `ResourceServerConfig.jwtDecoder(...)` crea `NimbusJwtDecoder` con JWK set de `issuer-uri + /protocol/openid-connect/certs`.
3. Se aplican validadores:
   - default con issuer (`JwtValidators.createDefaultWithIssuer`) 
   - validador custom de audience (`security.oauth2.audience`).
4. `KeycloakRealmRoleJwtAuthenticationConverter.convert(Jwt)`:
   - extrae `realm_access.roles`, normaliza uppercase y filtra a `{ADMIN, OWNER, TEACHER, STUDENT}`.
   - mapea a authorities `ROLE_*`.
   - construye `AuthenticatedUser(sub,email,realmRoles,authorities)`.
   - crea `JwtAuthenticationToken` con `name = sub` y coloca `AuthenticatedUser` en `authentication.details`.

### 2) Resolución de “usuario actual” (`me`)
1. `MeController.me()` invoca `SecurityContextFacade.getRequiredAuthenticatedUser()`.
2. `SecurityContextFacade` lee `SecurityContextHolder.getContext().getAuthentication()`.
3. Si no hay `authentication` válida o `details` no es `AuthenticatedUser`, lanza `ForbiddenException`.
4. `MeController` lee `academyId` desde `TenantContextHolder` (si existe).
5. Si usuario no admin y no hay tenant context → `ForbiddenException("Tenant context is required")`.
6. Responde `MeResponse(sub,email,authorities,academyId)`.

**Definición de “me” en código:**
- `sub` y `email` provienen del JWT validado (claims `sub`/`email`).
- `authorities` provienen de roles de realm soportados.
- `academyId` proviene de resolución de tenant durante el request (no de claim JWT).

### 3) Multi-tenancy (academy_id)
1. `TenantResolutionFilter` corre después del bearer token filter.
2. Determina si el usuario requiere tenant (`ROLE_OWNER|ROLE_TEACHER|ROLE_STUDENT`).
3. Si requiere:
   - `TenantSelection.resolveActiveAcademyId(request)` intenta `X-Academy-Id`; si no, cookie `academy_id`.
   - puede devolver `null` (si no seleccionado).
   - llama `TenantAccessPort.resolveAcademyId(authenticatedUser, selectedAcademyId)`.
   - guarda resultado en `TenantContextHolder` (`ThreadLocal<TenantContext>`).
4. Siempre limpia el `ThreadLocal` en `finally`.
5. `TenantEnforcementFilter` valida que usuarios no-admin tengan tenant context presente; si no, `ForbiddenException`.

### 4) Resolución de academy en identity
1. `TenantAccessPortAdapter.resolveAcademyId(...)` delega a `ResolveTenantContextUseCase.resolveAcademyId(sub, selectedAcademyId)`.
2. `ResolveTenantContextService.resolveAcademyId(...)`:
   - busca usuario por `keycloakSub`.
   - exige estado `IdentityUserStatus.ACTIVE`.
   - carga memberships activas por `userId`.
   - reglas:
     - 0 memberships activas → denied.
     - `selectedAcademyId` informado y no pertenece → denied.
     - 1 membership activa y sin selección → usa esa academy.
     - >1 memberships activas y sin selección → denied (requiere selección explícita).
3. `IdentityAccessDeniedException` es capturada por adapter y transformada a `ForbiddenException` (HTTP 403 vía handler global).

### 5) Manejo de errores / casos borde observables
- JWT sin `sub` o sin `email` → `NullPointer` via `Objects.requireNonNull` en `JwtClaimsExtractor`.
- JWT sin audience requerida → token inválido por `OAuth2TokenValidatorResult.failure`.
- Usuario no provisionado o inactivo → `IdentityAccessDeniedException` → `ForbiddenException`.
- Membership inexistente/inválida/ambigüa sin selección → `ForbiddenException`.
- Header/cookie academy con UUID mal formado → `IllegalArgumentException` (parse) manejada como `400` por `GlobalExceptionHandler`.
- Request no autenticado → `AuthenticationException`/fallo de security chain (401) y/o `ForbiddenException` si llega a facade sin auth.

---

## Diagrama de flujo (happy path) de `GET /api/identity/me`

### Mermaid (sequenceDiagram)

```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente HTTP
    participant SC as ResourceServerConfig
    participant BTF as BearerTokenAuthenticationFilter
    participant JD as ResourceServerConfig.jwtDecoder
    participant KRC as KeycloakRealmRoleJwtAuthenticationConverter
    participant TRF as TenantResolutionFilter
    participant TS as TenantSelection
    participant TAP as TenantAccessPortAdapter
    participant RTS as ResolveTenantContextService
    participant IURA as IdentityUserRepositoryAdapter
    participant SDRU as SpringDataIdentityUserRepository
    participant MRA as MembershipRepositoryAdapter
    participant SDRM as SpringDataMembershipRepository
    participant TCH as TenantContextHolder
    participant TEF as TenantEnforcementFilter
    participant MC as MeController
    participant SCF as SecurityContextFacade

    C->>SC: ResourceServerConfig.resourceServerFilterChain(...): registra chain + filtros tenant
    Note right of SC: Se configura anyRequest().authenticated(), jwt() con converter custom y orden de filtros.

    C->>BTF: BearerTokenAuthenticationFilter.doFilterInternal(...): extrae Bearer token
    BTF->>JD: JwtDecoder.decode(token): valida firma/JWK, issuer y audience
    Note right of JD: ResourceServerConfig.jwtDecoder(...): DelegatingOAuth2TokenValidator(issuer + audience).

    JD-->>BTF: Jwt válido
    BTF->>KRC: KeycloakRealmRoleJwtAuthenticationConverter.convert(jwt)
    Note right of KRC: Extrae realm_access.roles, filtra {ADMIN,OWNER,TEACHER,STUDENT}, crea ROLE_*, arma AuthenticatedUser y lo setea en Authentication.details.

    KRC-->>BTF: JwtAuthenticationToken(name=sub, details=AuthenticatedUser)
    BTF-->>TRF: Continúa chain autenticado

    TRF->>SCF: SecurityContextFacade.getRequiredAuthenticatedUser()
    SCF-->>TRF: AuthenticatedUser(sub,email,authorities)
    TRF->>TS: TenantSelection.resolveActiveAcademyId(request)
    Note right of TS: Prioridad: header X-Academy-Id; fallback cookie academy_id; parse UUID.

    TS-->>TRF: selectedAcademyId | null
    TRF->>TAP: TenantAccessPortAdapter.resolveAcademyId(authenticatedUser, selectedAcademyId)
    TAP->>RTS: ResolveTenantContextService.resolveAcademyId(sub, selectedAcademyId)

    RTS->>IURA: IdentityUserRepositoryAdapter.findByKeycloakSub(sub)
    IURA->>SDRU: SpringDataIdentityUserRepository.findByKeycloakSub(sub)
    SDRU-->>IURA: JpaIdentityUserEntity?
    IURA-->>RTS: IdentityUser?
    Note right of RTS: Valida user provisionado y status ACTIVE.

    RTS->>MRA: MembershipRepositoryAdapter.findActiveMembershipsByUserId(userId)
    MRA->>SDRM: SpringDataMembershipRepository.findByUserIdAndStatus(userId, ACTIVE)
    SDRM-->>MRA: List<JpaMembershipEntity>
    MRA-->>RTS: List<AcademyMembership>
    Note right of RTS: Resuelve academy: selección explícita válida, o única membership activa.

    RTS-->>TAP: academyId resuelto
    TAP-->>TRF: academyId resuelto
    TRF->>TCH: TenantContextHolder.set(new TenantContext(academyId))

    TRF-->>TEF: Continúa chain con tenant context
    TEF->>SCF: SecurityContextFacade.getRequiredAuthenticatedUser()
    SCF-->>TEF: AuthenticatedUser
    TEF->>TCH: TenantContextHolder.get()
    Note right of TEF: Si no es ROLE_ADMIN exige tenant context presente.

    TEF-->>MC: Invoca endpoint /api/identity/me
    MC->>SCF: SecurityContextFacade.getRequiredAuthenticatedUser()
    SCF-->>MC: AuthenticatedUser
    MC->>TCH: TenantContextHolder.get()
    Note right of MC: Si no admin y academyId null => ForbiddenException.

    MC-->>C: MeController.me() -> new MeResponse(sub,email,authorities,academyId)
    TRF->>TCH: TenantResolutionFilter.doFilterInternal(... finally ...): clear()
```

### Trazado narrado (happy path)

1. **Configuración previa de seguridad**: `ResourceServerConfig.resourceServerFilterChain(...)` construye el `SecurityFilterChain` de la aplicación, activa `oauth2ResourceServer().jwt(...)`, registra `KeycloakRealmRoleJwtAuthenticationConverter` y ubica `TenantResolutionFilter` y `TenantEnforcementFilter` después del `BearerTokenAuthenticationFilter`.
2. **Entrada del request**: al llegar `GET /api/identity/me` con token Bearer válido, `BearerTokenAuthenticationFilter.doFilterInternal(...)` extrae el token y delega la validación a `JwtDecoder.decode(...)`.
3. **Validación criptográfica y de claims base**: el decoder construido en `ResourceServerConfig.jwtDecoder(...)` usa `NimbusJwtDecoder` contra JWKS de Keycloak y valida `issuer` + `audience` requerida.
4. **Conversión JWT → Authentication**: `KeycloakRealmRoleJwtAuthenticationConverter.convert(Jwt)` transforma `realm_access.roles` en authorities `ROLE_*`, crea `AuthenticatedUser(sub,email,realmRoles,authorities)` y lo almacena en `Authentication.details`.
5. **Inicio de resolución tenant**: `TenantResolutionFilter.doFilterInternal(...)` obtiene el usuario actual mediante `SecurityContextFacade.getRequiredAuthenticatedUser()`.
6. **Selección de academy desde request**: `TenantSelection.resolveActiveAcademyId(...)` busca primero `X-Academy-Id`, luego cookie `academy_id` y parsea `UUID`.
7. **Delegación cross-módulo por puerto**: `TenantResolutionFilter` llama `TenantAccessPort.resolveAcademyId(...)`, cuya implementación es `TenantAccessPortAdapter.resolveAcademyId(...)` en `identity`.
8. **Caso de uso de identidad**: `ResolveTenantContextService.resolveAcademyId(...)` valida que el `sub` esté provisionado y con `IdentityUserStatus.ACTIVE`; para eso usa `IdentityUserRepositoryAdapter.findByKeycloakSub(...)` → `SpringDataIdentityUserRepository.findByKeycloakSub(...)`.
9. **Consulta de memberships activas**: el mismo caso de uso invoca `MembershipRepositoryAdapter.findActiveMembershipsByUserId(...)` → `SpringDataMembershipRepository.findByUserIdAndStatus(userId, ACTIVE)`.
10. **Regla de resolución**: con los datos anteriores, `ResolveTenantContextService` decide el `academyId` final (seleccionada explícita válida o única activa) y lo retorna.
11. **Contexto tenant en request**: `TenantResolutionFilter` persiste `new TenantContext(academyId)` en `TenantContextHolder.set(...)`.
12. **Enforcement tenant**: `TenantEnforcementFilter.doFilterInternal(...)` vuelve a leer usuario y tenant context; si el usuario no es admin, exige que exista contexto tenant.
13. **Controller del endpoint**: `MeController.me()` recupera `AuthenticatedUser` vía `SecurityContextFacade.getRequiredAuthenticatedUser()` y `academyId` vía `TenantContextHolder.get()`.
14. **Construcción de response DTO**: `MeController.me()` crea `new MeResponse(user.sub(), user.email(), user.authorities(), academyId)` y lo devuelve como `200 OK`.
15. **Limpieza del contexto**: al cerrar el chain, el `finally` de `TenantResolutionFilter.doFilterInternal(...)` ejecuta `TenantContextHolder.clear()` para evitar fuga de contexto entre requests.

---

## Documentación clase por clase (método por método)

> Nota: en `record`/`enum` se indican métodos explícitos y se aclara que los accessors/canonical constructor son generados por Java.

## Módulo `shared-security`

### 1) `TenantAccessPort`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/application/port/out/TenantAccessPort.java`
- **Paquete:** `com.sotium.shared.security.application.port.out`
- **Capa:** Aplicación (puerto de salida)
- **Responsabilidad:** Definir contrato para resolver academy tenant autorizada.
- **Dependencias:** ninguna (interfaz).
- **Métodos:**
  - `UUID resolveAcademyId(AuthenticatedUser authenticatedUser, UUID selectedAcademyId)`
    - **Qué hace:** firma del contrato.
    - **Reglas:** no define implementación.
    - **Errores:** no especifica.
    - **Efectos secundarios:** ninguno.
- **Justificación de paquete:** correcto como puerto de aplicación desacoplado de infra.

### 2) `AuthenticatedUser`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/domain/model/AuthenticatedUser.java`
- **Paquete:** `com.sotium.shared.security.domain.model`
- **Capa:** Dominio compartido (modelo de seguridad)
- **Responsabilidad:** transportar identidad autenticada normalizada.
- **Campos:** `sub`, `email`, `realmRoles`, `authorities`.
- **Métodos explícitos:** ninguno (record).
- **Errores/Efectos secundarios:** N/A.
- **Justificación:** DTO inmutable de frontera security↔aplicación.

### 3) `TenantContext`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/domain/model/TenantContext.java`
- **Paquete:** `com.sotium.shared.security.domain.model`
- **Capa:** Dominio compartido
- **Responsabilidad:** encapsular academy activa del request.
- **Métodos explícitos:** ninguno (record).

### 4) `JwtClaimsExtractor`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/security/JwtClaimsExtractor.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.security`
- **Capa:** Infraestructura (security utility)
- **Responsabilidad:** extraer claims obligatorios del JWT de forma fail-fast.
- **Dependencias:** `Jwt`, `Objects`.
- **Métodos:**
  - `static String sub(Jwt jwt)`
    - obtiene `jwt.getSubject()` y exige no-null.
    - lanza `NullPointerException` si falta claim.
  - `static String email(Jwt jwt)`
    - obtiene claim `email` y exige no-null.
    - lanza `NullPointerException` si falta.
- **Efectos secundarios:** ninguno.
- **Observación:** comportamiento fail-fast consistente; no customiza excepción de negocio.

### 5) `KeycloakRealmRoleJwtAuthenticationConverter`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/security/KeycloakRealmRoleJwtAuthenticationConverter.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.security`
- **Capa:** Infraestructura (adapter de seguridad)
- **Responsabilidad:** convertir JWT de Keycloak a `JwtAuthenticationToken` y `AuthenticatedUser`.
- **Dependencias:** `Jwt`, `JwtAuthenticationToken`, `GrantedAuthority`, `JwtClaimsExtractor`.
- **Métodos:**
  - `AbstractAuthenticationToken convert(Jwt jwt)`
    - extrae roles realm soportados.
    - genera authorities `ROLE_*`.
    - crea `AuthenticatedUser` con sub/email.
    - setea `AuthenticatedUser` en `authentication.details`.
    - puede propagar `NullPointerException` por claims faltantes.
  - `private Set<String> extractRealmRoles(Jwt jwt)`
    - lee claim map `realm_access` y lista `roles`.
    - filtra tipos String, uppercase, solo roles soportados.
    - retorna set inmutable.
- **Reglas:** whitelist explícita de roles soportados.
- **Efectos secundarios:** logging debug cuando no hay roles soportados.
- **Posible smell:** roles soportados hardcodeados; extensibilidad depende de cambio de código.

### 6) `ResourceServerConfig`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/security/ResourceServerConfig.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.security`
- **Capa:** Infraestructura/config
- **Responsabilidad:** configurar seguridad HTTP, JWT decoder y beans tenant context.
- **Dependencias inyectadas:** `HttpSecurity`, `TenantResolutionFilter`, `TenantEnforcementFilter`, properties de issuer/audience.
- **Métodos:**
  - `SecurityFilterChain resourceServerFilterChain(...)`
    - deshabilita CSRF.
    - permite rutas públicas: `/actuator/health/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/api/public/**`.
    - exige autenticación para el resto.
    - configura resource server JWT con converter custom.
    - agrega `TenantResolutionFilter` tras `BearerTokenAuthenticationFilter`.
    - agrega `TenantEnforcementFilter` tras `TenantResolutionFilter`.
  - `JwtDecoder jwtDecoder(String issuerUri, String audience)`
    - construye `NimbusJwtDecoder` contra JWK set URI de Keycloak.
    - valida issuer y audience requerida.
  - `TenantContextHolder tenantContextHolder()`
    - expone bean para contexto tenant ThreadLocal.
- **Errores:** token inválido por issuer/audience produce auth failure.
- **Justificación:** correcto en infra; concentra detalles Spring Security.

### 7) `SecurityContextFacade`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/security/SecurityContextFacade.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.security`
- **Capa:** Infraestructura (adapter para SecurityContext)
- **Responsabilidad:** acceso tipado al usuario autenticado actual.
- **Métodos:**
  - `AuthenticatedUser getRequiredAuthenticatedUser()`
    - lee `SecurityContextHolder`.
    - exige `authentication` no-null y autenticado.
    - exige `authentication.details` de tipo `AuthenticatedUser`.
    - lanza `ForbiddenException` si no cumple.
- **Efectos secundarios:** ninguno.

### 8) `ForbiddenException`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/security/exceptions/ForbiddenException.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.security.exceptions`
- **Capa:** Infraestructura/shared exception
- **Responsabilidad:** representar denegación de acceso aplicada por la capa security/app.
- **Métodos:** constructor `ForbiddenException(String message)`.

### 9) `TenantContextHolder`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantContextHolder.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.web.filter`
- **Capa:** Infraestructura web
- **Responsabilidad:** almacenar contexto tenant por hilo/request.
- **Métodos:**
  - `void set(TenantContext context)`
  - `Optional<TenantContext> get()`
  - `void clear()`
- **Efectos secundarios:** manipula `ThreadLocal`; requiere limpieza (se cumple en filter).

### 10) `TenantEnforcementFilter`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantEnforcementFilter.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.web.filter`
- **Capa:** Adapter-In (web filter)
- **Responsabilidad:** exigir existencia de tenant context para no-admin.
- **Dependencias:** `TenantContextHolder`, `SecurityContextFacade`.
- **Métodos:**
  - `doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)`
    - obtiene user autenticado.
    - calcula si es admin por authority `ROLE_ADMIN`.
    - si no-admin y tenant vacío, lanza `ForbiddenException`.
    - continúa chain en caso válido.
  - `shouldNotFilter(HttpServletRequest)`
    - excluye `/actuator`, `/swagger-ui`, `/v3/api-docs`, `/api/public`.
- **Efectos secundarios:** logging warn en faltante de tenant.

### 11) `TenantResolutionFilter`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantResolutionFilter.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.web.filter`
- **Capa:** Adapter-In (web filter)
- **Responsabilidad:** resolver tenant por request antes del enforcement.
- **Dependencias:** `SecurityContextFacade`, `TenantSelection`, `TenantAccessPort`, `TenantContextHolder`.
- **Métodos:**
  - `doFilterInternal(...)`
    - obtiene usuario autenticado.
    - determina si role requiere tenant (`OWNER/TEACHER/STUDENT`).
    - extrae academy seleccionada (header/cookie).
    - delega validación/resolución a `TenantAccessPort`.
    - setea `TenantContext`.
    - en `finally`, limpia `TenantContextHolder` siempre.
  - `shouldNotFilter(...)`
    - mismas exclusiones que enforcement.
- **Errores:** propaga `IllegalArgumentException` por UUID inválido o `ForbiddenException` desde puerto/adaptador.

### 12) `TenantSelection`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/infrastructure/web/filter/TenantSelection.java`
- **Paquete:** `com.sotium.shared.security.infrastructure.web.filter`
- **Capa:** Infra web helper
- **Responsabilidad:** resolver academy seleccionada desde metadata request.
- **Métodos:**
  - `Optional<UUID> resolveActiveAcademyId(HttpServletRequest request)`
    - prioridad 1: header `X-Academy-Id`.
    - prioridad 2: cookie `academy_id`.
    - parsea `UUID.fromString`.
- **Errores:** `IllegalArgumentException` en UUID malformado.

### 13) `ApiError`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/interfaces/rest/ApiError.java`
- **Paquete:** `com.sotium.shared.security.interfaces.rest`
- **Capa:** Interfaces/REST
- **Responsabilidad:** payload de error API uniforme.
- **Métodos explícitos:** ninguno (record).

### 14) `GlobalExceptionHandler`
- **Path:** `shared-security/src/main/java/com/sotium/shared/security/interfaces/rest/GlobalExceptionHandler.java`
- **Paquete:** `com.sotium.shared.security.interfaces.rest`
- **Capa:** Adapter-In REST transversal
- **Responsabilidad:** mapear excepciones a respuestas HTTP.
- **Métodos:**
  - `handleForbidden(RuntimeException, HttpServletRequest)`
    - captura `AccessDeniedException` y `ForbiddenException`.
    - responde 403 con `ApiError`.
  - `handleUnauthorized(AuthenticationException, HttpServletRequest)`
    - responde 401.
  - `handleBadRequest(IllegalArgumentException, HttpServletRequest)`
    - responde 400.
  - `build(HttpStatus, String, String)` (privado)
    - construye `ApiError` con `OffsetDateTime.now()`.
- **Efectos secundarios:** logs warn en 401/403.

---

## Módulo `identity`

### 1) `IdentityAccessDeniedException`
- **Path:** `identity/src/main/java/com/sotium/identity/application/exception/IdentityAccessDeniedException.java`
- **Paquete:** `com.sotium.identity.application.exception`
- **Capa:** Aplicación
- **Responsabilidad:** error de acceso denegado específico del caso de uso identity.
- **Métodos:** constructor `IdentityAccessDeniedException(String message)`.

### 2) `ResolveTenantContextUseCase`
- **Path:** `identity/src/main/java/com/sotium/identity/application/port/in/ResolveTenantContextUseCase.java`
- **Paquete:** `com.sotium.identity.application.port.in`
- **Capa:** Aplicación (puerto de entrada)
- **Métodos:**
  - `UUID resolveAcademyId(String keycloakSub, UUID selectedAcademyId)`.

### 3) `IdentityUserRepository`
- **Path:** `identity/src/main/java/com/sotium/identity/application/port/out/IdentityUserRepository.java`
- **Paquete:** `com.sotium.identity.application.port.out`
- **Capa:** Aplicación (puerto de salida)
- **Métodos:**
  - `Optional<IdentityUser> findByKeycloakSub(String keycloakSub)`.

### 4) `MembershipRepository`
- **Path:** `identity/src/main/java/com/sotium/identity/application/port/out/MembershipRepository.java`
- **Paquete:** `com.sotium.identity.application.port.out`
- **Capa:** Aplicación (puerto de salida)
- **Métodos:**
  - `List<AcademyMembership> findActiveMembershipsByUserId(UUID userId)`.

### 5) `ResolveTenantContextService`
- **Path:** `identity/src/main/java/com/sotium/identity/application/usecase/ResolveTenantContextService.java`
- **Paquete:** `com.sotium.identity.application.usecase`
- **Capa:** Aplicación (use case)
- **Responsabilidad:** orquestar reglas de acceso tenant con identidad y memberships.
- **Dependencias:** `IdentityUserRepository`, `MembershipRepository`.
- **Métodos:**
  - `UUID resolveAcademyId(String keycloakSub, UUID selectedAcademyId)`
    - busca user por `keycloakSub`.
    - exige `status == ACTIVE`.
    - exige memberships activas no vacías.
    - si selección explícita, valida pertenencia.
    - si una sola membership activa y sin selección, autoselecciona.
    - si múltiples y sin selección, rechaza.
    - lanza `IdentityAccessDeniedException` con mensajes específicos por caso.
- **Efectos secundarios:** logs warn en rutas denegadas.
- **Justificación:** correcto en aplicación, sin dependencias de infraestructura.

### 6) `IdentityUser`
- **Path:** `identity/src/main/java/com/sotium/identity/domain/model/IdentityUser.java`
- **Paquete:** `com.sotium.identity.domain.model`
- **Capa:** Dominio
- **Responsabilidad:** representar usuario de identidad provisionado.
- **Métodos explícitos:** ninguno (record).

### 7) `IdentityUserStatus`
- **Path:** `identity/src/main/java/com/sotium/identity/domain/model/IdentityUserStatus.java`
- **Paquete:** `com.sotium.identity.domain.model`
- **Capa:** Dominio
- **Valores:** `ACTIVE`, `INVITED`, `DISABLED`.

### 8) `AcademyMembership`
- **Path:** `identity/src/main/java/com/sotium/identity/domain/model/AcademyMembership.java`
- **Paquete:** `com.sotium.identity.domain.model`
- **Capa:** Dominio
- **Responsabilidad:** representar membresía user↔academy con rol/estado.
- **Métodos:**
  - `boolean isActive()`
    - retorna `status == MembershipStatus.ACTIVE`.

### 9) `MembershipRole`
- **Path:** `identity/src/main/java/com/sotium/identity/domain/model/MembershipRole.java`
- **Paquete:** `com.sotium.identity.domain.model`
- **Capa:** Dominio
- **Valores:** `OWNER`, `TEACHER`, `STUDENT`.

### 10) `MembershipStatus`
- **Path:** `identity/src/main/java/com/sotium/identity/domain/model/MembershipStatus.java`
- **Paquete:** `com.sotium.identity.domain.model`
- **Capa:** Dominio
- **Valores:** `ACTIVE`, `INVITED`, `DISABLED`.

### 11) `IdentityUserRepositoryAdapter`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/IdentityUserRepositoryAdapter.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Adapter-Out (infra persistence)
- **Responsabilidad:** implementar puerto `IdentityUserRepository` con Spring Data.
- **Dependencias:** `SpringDataIdentityUserRepository`.
- **Métodos:**
  - `Optional<IdentityUser> findByKeycloakSub(String keycloakSub)`
    - consulta Spring Data y mapea con `PersistenceMappers.toDomain`.
- **Efectos secundarios:** acceso DB.

### 12) `JpaIdentityUserEntity`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/JpaIdentityUserEntity.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Infraestructura persistence
- **Responsabilidad:** entidad JPA tabla `identity_users`.
- **Campos:** `id`, `keycloakSub` (unique), `email` (unique), `status`.
- **Métodos explícitos:** ninguno (Lombok getters + ctor protegido).

### 13) `JpaMembershipEntity`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/JpaMembershipEntity.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Infraestructura persistence
- **Responsabilidad:** entidad JPA tabla `academy_memberships`.
- **Campos:** `id`, `academyId`, `userId`, `role`, `status`.

### 14) `MembershipRepositoryAdapter`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/MembershipRepositoryAdapter.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Adapter-Out (infra persistence)
- **Responsabilidad:** implementar puerto `MembershipRepository` con Spring Data.
- **Dependencias:** `SpringDataMembershipRepository`.
- **Métodos:**
  - `List<AcademyMembership> findActiveMembershipsByUserId(UUID userId)`
    - consulta por `userId` + `MembershipStatus.ACTIVE`.
    - mapea entidades a dominio.
- **Efectos secundarios:** acceso DB.

### 15) `PersistenceMappers`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/PersistenceMappers.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Infraestructura helper
- **Responsabilidad:** mapeos entidad JPA → dominio.
- **Métodos:**
  - `static IdentityUser toDomain(JpaIdentityUserEntity entity)`
  - `static AcademyMembership toDomain(JpaMembershipEntity entity)`
- **Efectos secundarios:** ninguno.

### 16) `SpringDataIdentityUserRepository`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/SpringDataIdentityUserRepository.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Infraestructura persistence
- **Responsabilidad:** repositorio JPA para `JpaIdentityUserEntity`.
- **Métodos:**
  - `Optional<JpaIdentityUserEntity> findByKeycloakSub(String keycloakSub)`.

### 17) `SpringDataMembershipRepository`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/persistence/SpringDataMembershipRepository.java`
- **Paquete:** `com.sotium.identity.infrastructure.persistence`
- **Capa:** Infraestructura persistence
- **Responsabilidad:** repositorio JPA para `JpaMembershipEntity`.
- **Métodos:**
  - `List<JpaMembershipEntity> findByUserIdAndStatus(UUID userId, MembershipStatus status)`.

### 18) `TenantAccessPortAdapter`
- **Path:** `identity/src/main/java/com/sotium/identity/infrastructure/security/TenantAccessPortAdapter.java`
- **Paquete:** `com.sotium.identity.infrastructure.security`
- **Capa:** Adapter-Out hacia shared-security
- **Responsabilidad:** bridge entre `TenantAccessPort` y caso de uso de identity.
- **Dependencias:** `ResolveTenantContextUseCase`.
- **Métodos:**
  - `UUID resolveAcademyId(AuthenticatedUser authenticatedUser, UUID selectedAcademyId)`
    - delega a `resolveTenantContextUseCase.resolveAcademyId(authenticatedUser.sub(), selectedAcademyId)`.
    - traduce `IdentityAccessDeniedException` a `ForbiddenException`.
- **Efectos secundarios:** logging warn en denegación.

### 19) `MeController`
- **Path:** `identity/src/main/java/com/sotium/identity/interfaces/web/MeController.java`
- **Paquete:** `com.sotium.identity.interfaces.web`
- **Capa:** Adapter-In REST
- **Responsabilidad:** endpoint autenticado `/api/identity/me`.
- **Dependencias:** `SecurityContextFacade`, `TenantContextHolder`.
- **Métodos:**
  - `MeResponse me()`
    - obtiene usuario actual.
    - obtiene `academyId` del tenant context.
    - exige tenant para no-admin.
    - retorna `MeResponse`.
- **Errores:** `ForbiddenException` si falta contexto tenant para no-admin.

### 20) `MeResponse`
- **Path:** `identity/src/main/java/com/sotium/identity/interfaces/web/MeResponse.java`
- **Paquete:** `com.sotium.identity.interfaces.web`
- **Capa:** Interfaces DTO
- **Responsabilidad:** respuesta del endpoint `me`.
- **Campos:** `sub`, `email`, `authorities`, `academyId`.

### 21) `PublicIdentityController`
- **Path:** `identity/src/main/java/com/sotium/identity/interfaces/web/PublicIdentityController.java`
- **Paquete:** `com.sotium.identity.interfaces.web`
- **Capa:** Adapter-In REST público
- **Responsabilidad:** endpoint público de probe para academy registration.
- **Métodos:**
  - `ResponseEntity<Map<String, String>> registrationProbe()`
    - retorna `{"status": "registration endpoint available"}`.
- **Efectos secundarios:** logging debug.

### 22) `ResolveTenantContextServiceTest` (test)
- **Path:** `identity/src/test/java/com/sotium/identity/application/usecase/ResolveTenantContextServiceTest.java`
- **Paquete:** `com.sotium.identity.application.usecase`
- **Capa:** Test de aplicación
- **Responsabilidad:** validar reglas base del caso de uso.
- **Métodos test:**
  - `givenValidActiveUserWithSingleMembership_whenNoTenantSelected_thenReturnMembershipAcademy()`
    - verifica resolución automática con única membership activa.
  - `givenJwtSubjectNotProvisioned_whenResolveTenant_thenThrowIdentityAccessDenied()`
    - verifica rechazo cuando usuario no existe.

### 23) `application.yml` (`identity`)
- **Path:** `identity/src/main/resources/application.yml`
- **Tipo:** recurso de configuración
- **Contenido observable:** `spring.application.name: identity`.
- **No verificable:** no contiene propiedades de datasource/security en este archivo.

---

## Decisiones de seguridad verificadas

1. **Dónde se valida JWT:**
   - En `ResourceServerConfig` mediante `JwtDecoder` (`NimbusJwtDecoder`) + validadores issuer/audience.
2. **Cómo se construye principal/authorities:**
   - `KeycloakRealmRoleJwtAuthenticationConverter` toma roles de `realm_access.roles`, filtra whitelist, construye `ROLE_*` y setea `AuthenticatedUser` en `Authentication.details`.
3. **Dónde se aplica autorización:**
   - A nivel HTTP (`anyRequest().authenticated()`) en `SecurityFilterChain`.
   - A nivel tenant por filtros (`TenantResolutionFilter` + `TenantEnforcementFilter`).
   - En endpoint `me` hay chequeo adicional explícito para no-admin sin tenant.
4. **Manejo de forbidden/unauthorized/bad request:**
   - `GlobalExceptionHandler` mapea a 403/401/400.

---

## Smells y riesgos de acoplamiento observados (accionables)

1. **`ThreadLocal` tenant context**
   - Mitigación actual: `clear()` en `finally` de `TenantResolutionFilter` (correcto).
   - Riesgo residual: uso fuera del ciclo request-thread (no evidenciado en código actual).

2. **Dependencia del `Authentication.details` para `AuthenticatedUser`**
   - Si otro converter/filtro sobreescribe `details`, `SecurityContextFacade` falla con 403.
   - Acción: mantener contrato explícito/documentado en security config.

3. **Uso de `NullPointerException` para claims faltantes (`sub/email`)**
   - Funciona fail-fast, pero error semántico podría ser menos claro para cliente.
   - Acción opcional: mapear a excepción de autenticación explícita si se desea diagnóstico más controlado.

4. **Roles soportados hardcoded en converter**
   - Cualquier rol nuevo requiere deploy.
   - Acción: parametrizar whitelist por properties (si el equipo lo desea).

5. **`IllegalArgumentException` por UUID malformado en header/cookie**
   - Actualmente se mapea a 400 (correcto y explícito).

---

## Qué probar (plan de validación accionable)

1. **JWT válido + audience correcta + rol OWNER + 1 membership activa + sin header/cookie**
   - Esperado: `GET /api/identity/me` retorna 200 con `academyId` de membership.
2. **JWT válido + rol OWNER + múltiples memberships + sin selección**
   - Esperado: 403 con mensaje de selección explícita requerida.
3. **JWT válido + rol OWNER + `X-Academy-Id` no perteneciente**
   - Esperado: 403.
4. **JWT válido + rol ADMIN + sin tenant**
   - Esperado: pasa filtros tenant y `me` permite `academyId = null`.
5. **JWT con audience incorrecta**
   - Esperado: rechazo de autenticación (401 por resource server).
6. **Header `X-Academy-Id` malformado**
   - Esperado: 400 por `IllegalArgumentException`.
7. **Usuario no provisionado o `IdentityUserStatus != ACTIVE`**
   - Esperado: 403 por adaptación `IdentityAccessDeniedException -> ForbiddenException`.

---

## Checklist final de calidad

- [x] No hay nombres inventados: todo sale del código inspeccionado.
- [x] La documentación incluye todas las clases encontradas en `identity` y `shared-security` (incluye clase de test en `identity`).

