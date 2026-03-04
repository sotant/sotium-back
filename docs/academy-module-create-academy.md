# Implementación base del módulo `academy` (dominio + aplicación + persistencia)

## Por qué se realizó este cambio

Se necesitaba pasar de una estructura vacía a un módulo `academy` funcional a nivel interno, con límites hexagonales claros y alineación explícita con el esquema base (`academies`, `academy_settings`).

El objetivo fue habilitar el caso de uso de creación de academia para consumo posterior desde `onboarding`, sin introducir todavía lógica de deduplicación ni endpoints REST.

## Qué se modificó

- Se implementó el dominio `Academy` con invariantes de nombre, email, estado y fecha de creación.
- Se implementó `AcademySettings` con valores por defecto compatibles con DB:
  - `timezone = "UTC"`
  - `openingHours = {}`
  - `holidays = []`
- Se definieron puertos de aplicación (`AcademyRepositoryPort`, `AcademySettingsRepositoryPort`) y puerto público de integración (`AcademyRegistrationPort`).
- Se implementó el caso de uso `CreateAcademyService` con `CreateAcademyCommand` y `CreateAcademyResult`.
- Se añadieron entidades JPA internas del módulo:
  - `AcademyJpaEntity` → `academies`
  - `AcademySettingsJpaEntity` → `academy_settings`
- Se añadieron adaptadores de persistencia JPA y mapeadores entre dominio e infraestructura.
- Se añadió configuración del módulo para exponer `AcademyRegistrationPort` como bean cableable desde `bootstrap`.
- Se añadió `README.md` del módulo `academy` con responsabilidades, ownership de tablas y contrato expuesto.

## Impacto relevante

### Arquitectura

- El módulo `academy` deja de ser un esqueleto y pasa a ser un bounded context funcional con separación `domain/application/infrastructure`.
- Se mantiene aislamiento de contexto: sin acceso a repositorios de otros módulos y sin exposición de entidades JPA fuera de infraestructura.

### Persistencia y datos

- La implementación queda alineada con el esquema base existente para `academies` y `academy_settings`.
- No se introducen claves foráneas cross-módulo.
- No se añadió lógica de deduplicación/idempotencia en esta iteración.

### Testing

- Se agregaron tests unitarios de dominio.
- Se agregó test unitario del caso de uso sin Spring.
- Se agregó test de integración de persistencia JPA con PostgreSQL Testcontainers.
