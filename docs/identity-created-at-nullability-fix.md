# Corrección de validación JPA para `user_profiles.created_at`

## Por qué

Al arrancar la aplicación, Hibernate detenía la creación del `EntityManagerFactory` por una discrepancia de nulabilidad entre el modelo JPA y el esquema real: la columna `created_at` en `user_profiles` está definida como `NOT NULL` en base de datos, pero la entidad la declaraba implícitamente como nullable.

## Qué se modificó

- Se ajustó el mapeo de `created_at` en `JpaUserProfileEntity` para declarar explícitamente `nullable = false`, manteniendo `insertable = false` y `updatable = false`.
- Se incrementó la versión del proyecto con un bump **PATCH** por tratarse de una corrección compatible.
- Se añadió la entrada correspondiente en `CHANGELOG.md`.

## Impacto

- **Arquitectura**: sin cambios de capas ni contratos.
- **API**: sin cambios de endpoints ni payloads.
- **Persistencia**: se alinea el contrato de nulabilidad del modelo con el esquema existente, evitando el fallo de arranque por validación de esquema.
