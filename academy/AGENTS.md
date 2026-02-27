# AGENTS.md - academy

## Alcance
Estas reglas aplican a todo el módulo `academy`.

## Reglas específicas del contexto
- Mantener aislamiento del bounded context `academy`.
- No reutilizar entidades JPA de otros módulos.
- La capa `domain` no puede depender de Spring/JPA.
- La capa `application` solo define puertos y casos de uso.
- Adaptadores de infraestructura deben implementar puertos de salida.
- Endpoints deben depender de puertos de entrada, nunca de infraestructura.
- Convenciones de nombres:
  - Puertos: `...Repository`, `...UseCase`.
  - Adaptadores: `...Adapter`.
  - Entidades JPA: `Jpa...Entity`.

## Testing
- Domain/Application: tests unitarios sin Spring.
- Infrastructure: tests de integración solo cuando haya comportamiento adicional.
