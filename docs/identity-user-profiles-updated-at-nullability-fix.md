# Corrección de nulabilidad en `user_profiles.updated_at`

## Por qué

El arranque de la aplicación fallaba durante la validación de esquema de Hibernate porque la columna `user_profiles.updated_at` está definida como `NOT NULL` en base de datos, mientras que el mapeo JPA la declaraba implícitamente nullable.

Este desalineamiento provocaba `SchemaManagementException` y, en cascada, impedía inicializar `EntityManagerFactory` y los beans dependientes de persistencia en `identity`.

## Qué se modificó

- Se actualizó el mapeo JPA de `JpaUserProfileEntity` para declarar `updated_at` con `nullable = false`, manteniendo `insertable = false` y `updatable = false`.
- Se incrementó la versión del proyecto con bump PATCH por corrección compatible.
- Se registró el cambio en `CHANGELOG.md`.

## Impacto

- **Arquitectura**: sin cambios de diseño ni de límites entre módulos.
- **API**: sin cambios de contrato.
- **Persistencia**: el modelo JPA queda alineado con la restricción `NOT NULL` existente en la tabla `user_profiles`.
