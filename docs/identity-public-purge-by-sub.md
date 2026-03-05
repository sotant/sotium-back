# Endpoint público para purga por `sub` en identity

## Por qué

Se necesitaba una operación de soporte para eliminar de forma explícita la información de onboarding asociada a un usuario identificado por `sub`.

El objetivo es permitir una limpieza controlada desde el módulo `identity` sin exponer lógica de infraestructura en el controlador y manteniendo la orquestación en un caso de uso transaccional.

## Qué se modificó

- Se añadió el endpoint público `POST /api/public/identity/purge-by-sub` en `PublicIdentityController`.
- Se creó el caso de uso `DeleteIdentityBySubUseCase` y su implementación `DeleteIdentityBySubService`.
- La operación elimina, en este orden lógico dentro de la misma transacción:
  1. Membresías del usuario.
  2. Identidad (`identity_users`) del usuario.
  3. Academias relacionadas y sus settings.
- Se ampliaron puertos/adaptadores de persistencia para soportar borrado por `userId` y borrado de academias/settings por `academyId`.
- Se añadieron tests unitarios del caso de uso para escenarios de borrado efectivo y no-op cuando el `sub` no existe.

## Impacto

- **API**: nuevo endpoint público en `identity`.
- **Arquitectura**: integración entre módulos mediante puertos de aplicación (`academy`), manteniendo aislamiento de infraestructura.
- **Datos**: elimina registros en `academy_memberships`, `identity_users`, `academy_settings` y `academies` para academias asociadas al `sub` solicitado.
