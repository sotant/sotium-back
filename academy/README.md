# Módulo `academy`

## Responsabilidad

El módulo `academy` es el bounded context responsable de modelar el dominio de academias y su configuración inicial.

Su alcance actual cubre:

- Creación de academia (`Academy`).
- Creación de configuración inicial (`AcademySettings`).
- Exposición de un puerto de aplicación para registro desde flujos externos (por ejemplo, onboarding).

## Ownership de datos

Este módulo es dueño exclusivo de:

- `academies`
- `academy_settings`

## Puerto expuesto hacia otros módulos

El contrato público de integración de este módulo es:

- `AcademyRegistrationPort`
  - `CreateAcademyResult createAcademy(CreateAcademyCommand command)`

Este puerto permite registrar academias sin exponer entidades JPA ni repositorios internos.
