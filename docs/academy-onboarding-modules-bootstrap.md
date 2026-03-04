# Alta de estructura base para módulos `academy` y `onboarding`

## Por qué se realizó este cambio

Se necesitaba preparar el monolito modular para soportar el flujo de registro de academias con límites de contexto explícitos, sin introducir todavía lógica funcional.

Este ajuste permite que la base de arquitectura hexagonal quede lista para evolucionar el onboarding actual y abrir el camino al registro de usuarios futuro sin rediseñar la estructura modular.

## Qué se modificó

- Se crearon los nuevos módulos Maven `academy` y `onboarding`.
- Se generó la estructura de paquetes hexagonales para ambos módulos:
  - `domain`
  - `application`
  - `infrastructure`
  - `interfaces`
- Se añadieron `pom.xml` propios para ambos módulos con dependencias base.
- Se registraron ambos módulos en el `pom.xml` raíz y en `dependencyManagement`.
- Se vinculó `bootstrap` con los nuevos módulos para que queden incluidos en la composición de la aplicación.
- Se incrementó la versión del proyecto a `0.2.1-SNAPSHOT` en los POMs del repositorio.

## Impacto relevante

### Arquitectura

- Se refuerza la separación por bounded context, incorporando explícitamente `academy` (dominio académico) y `onboarding` (orquestación transversal).
- Se mantiene la dirección de dependencias propia del monolito modular.

### API / Lógica funcional

- No se añadieron endpoints, casos de uso ni reglas de negocio.
- El cambio es exclusivamente estructural.

### Operación

- Sin impacto operativo directo en comportamiento de negocio.
- El impacto principal es de organización del código y preparación para iteraciones futuras.
