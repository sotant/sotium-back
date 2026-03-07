# Endpoint público para listar usuarios de una academia (`identity`)

## Por qué

Se necesitaba una consulta pública para recuperar los usuarios pertenecientes a una academia concreta desde integraciones externas, devolviendo datos de perfil y email en una única respuesta.

## Qué se cambió

- Se añadió `GET /api/public/identity/users` con parámetro `academyId`.
- Se incorporó el caso de uso `ListAcademyUsersPublicUseCase` con implementación `ListAcademyUsersPublicService`.
- El caso de uso:
  1. Busca membresías por academia en `academy_memberships`.
  2. Resuelve cada `userId` contra `user_profiles`.
  3. Enriquce la respuesta con `email` desde `identity_users`.
- Se amplió el puerto de membresías para soportar búsquedas por academia.
- Se amplió el puerto de identidad para resolver usuario por `id`.
- Se añadieron tests unitarios del nuevo caso de uso y cobertura web del endpoint en el controller test.

## Impacto

- **API**: nuevo endpoint público de lectura de usuarios por academia.
- **Arquitectura**: mantiene separación hexagonal (controller → use case → puertos).
- **Datos**: no añade tablas; reutiliza `academy_memberships`, `user_profiles` e `identity_users`.
