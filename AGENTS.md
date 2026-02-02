GPT CODEX
# AGENTS.md

> Este archivo define **cómo deben comportarse los agentes**, **qué decisiones pueden tomar** y **qué reglas arquitectónicas son innegociables**.

Proyecto backend desarrollado con **Java 21**, **Spring Boot 4.x**, y **Arquitectura Hexagonal / Clean Architecture**, diseñado como **monolito modular preparado para futura extracción a microservicios**.

---

## 1. Objetivo

Este documento garantiza que cualquier agente de IA:

- Mantenga **consistencia técnica y arquitectónica**.
- Proteja la **estabilidad del dominio y los contratos públicos**.
- Evite deuda técnica y decisiones irreversibles.
- Trabaje de forma **segura, incremental y trazable**.

Este archivo **NO es un tutorial**, es un **contrato operativo obligatorio**.

---

## 2. Prioridad de instrucciones (OBLIGATORIA)

Ante cualquier conflicto, los agentes DEBEN seguir este orden estricto:

1. **AGENTS.md** (este archivo).
2. Instrucciones explícitas del usuario en la conversación actual.
3. `AGENTS.override.md` o `AGENTS.md` más cercanos en el árbol de directorios.
4. Código existente y tests del repositorio.
5. Convenciones oficiales de Java / Spring y buenas prácticas generales.

> Si el conflicto no puede resolverse con este orden → **DETENERSE Y PREGUNTAR. NO ASUMIR.**

---

## 3. Alcance y límites de los agentes

### Los agentes DEBEN:
- Ejecutar tareas técnicas **explícitamente solicitadas**.
- Realizar cambios **mínimos, seguros e incrementales**.
- Respetar arquitectura, contratos y límites de módulo.
- Priorizar claridad, legibilidad y mantenibilidad.
- Proponer alternativas ante decisiones abiertas y **esperar confirmación**.

### Los agentes NO deben:
- Tomar decisiones de negocio o producto.
- Redefinir arquitectura, dominios o contratos públicos.
- Introducir dependencias, frameworks o cambios infraestructurales sin aprobación.
- Realizar refactors especulativos.
- Dejar código parcialmente funcional o sin tests cuando sean requeridos.

---

## 4. Visión arquitectónica de alto nivel

- El sistema es **un monolito de despliegue único**.
- Internamente es **estrictamente modular**.
- Cada módulo es un **candidato real a microservicio**.

> Los microservicios son una decisión de despliegue.  
> **La arquitectura ya está preparada desde hoy.**

---

## 5. Principios técnicos obligatorios

Todos los agentes DEBEN respetar:

- Arquitectura Hexagonal (Ports & Adapters)
- Clean Architecture
- Bounded Contexts
- KISS
- DRY
- SOLID (especialmente SRP y DIP)
- Fail fast
- Explicit over implicit
- Frameworks como detalles

El dominio **NO depende** de frameworks ni de infraestructura.

---

## 6. Reglas arquitectónicas no negociables

- Dirección de dependencias estricta:
  infrastructure → application → domain
- El dominio es **Java puro**.
- No compartir entidades JPA entre módulos.
- No acceder a repositorios de otro módulo.
- No compartir tablas ni claves foráneas entre módulos.
- Comunicación entre módulos **solo vía puertos o eventos**.
- Seguridad **obligatoria**, nunca desactivada sin aprobación explícita.

> **Las violaciones de arquitectura son bugs.**

---

## 7. Seguridad y datos sensibles

- Autenticación y autorización obligatorias.
- Nunca exponer tokens, credenciales o datos personales.
- No registrar información sensible en logs.
- Validar permisos en endpoints sensibles.
- Mensajes de error claros pero seguros.

---

## 8. Testing (expectativas globales)

- Domain y Application testeables **sin Spring**.
- Infrastructure puede usar Spring Test.
- Tests deterministas y mantenibles.
- Evitar mocks que oculten integraciones críticas.

---

## 9. Convenciones generales

- Código y comentarios: **inglés**.
- Documentación: **español**.
- Preferir inmutabilidad, `final` y `record`.
- Constructor injection obligatorio.
- Prohibido `@Autowired` en campos.
- No usar `Optional` como parámetro.
- Evitar util classes genéricas sin tests.

---

## 10. Documentación y trazabilidad

- Documentar cambios relevantes en `/docs`, PR y `CHANGELOG.md`.
- Versionado **SemVer** obligatorio.
- CHANGELOG siguiendo:
  https://keepachangelog.com/es-ES/1.1.0/

Los agentes NO deben:
- Modificar versiones pasadas del changelog.
- Inventar cambios.
- Omitir impactos relevantes.

---

## 11. Regla final

Ante cualquier ambigüedad:

> **DETENTE. PREGUNTA. NO ASUMAS.**

Este archivo es **autoridad absoluta** para todos los agentes de IA.

---

### Nota

Las reglas específicas de módulo, persistencia (Flyway), mensajería, etc.  
**DEBEN definirse en `AGENTS.md` dentro del directorio correspondiente**.
