# Endpoint público para alta de usuarios (`identity`)

## Por qué

Se necesitaba un endpoint público para registrar usuarios desde procesos externos de onboarding sin depender de autenticación previa. El flujo debía crear de forma transaccional la identidad, el perfil y la membresía inicial del usuario en una academia.

## Qué se cambió

- Se añadió `POST /api/public/identity/register-user` en `PublicIdentityController`.
- Se incorporó el caso de uso `RegisterPublicUserUseCase` con implementación `RegisterPublicUserService`.
- El caso de uso crea:
  1. Registro en `identity_users` con:
     - `keycloak_sub` aleatorio (`UUID`),
     - `email` recibido,
     - estado `ACTIVE`.
  2. Registro en `user_profiles` con datos de la petición y valores mock cuando faltan:
     - `name` → `Mock Name`
     - `surname` → `Mock Surname`
     - `phone` → `000000000`
     - `avatarUrl` → `https://mock.sotium/avatar/default`
     - `bio` → `Mock bio`
  3. Registro en `academy_memberships` con:
     - academia de la petición,
     - rol hardcode `USER`,
     - estado `ACTIVE`.
- Se añadieron adaptadores/repositorios de persistencia para `user_profiles`.
- Se añadió cobertura de tests unitarios para el nuevo caso de uso.

## Impacto

- **API**: nuevo endpoint público para alta de usuarios.
- **Arquitectura**: se mantiene el patrón hexagonal con puertos de entrada/salida y lógica transaccional en capa Application.
- **Datos**: se escriben registros adicionales en `user_profiles` durante el alta pública de usuario.
