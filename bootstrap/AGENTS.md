# AGENTS.md - Bootstrap Module

> Este archivo define las reglas específicas para el módulo **bootstrap**.

## 1. Responsabilidad del Módulo

El módulo `bootstrap` actúa como el **Composition Root** de la aplicación.
Sus responsabilidades son:

- **Punto de entrada**: Contiene la clase `main` (`@SpringBootApplication`).
- **Configuración global**: Define beans de infraestructura compartida (seguridad, transacciones, etc.).
- **Orquestación**: Carga y conecta los módulos de negocio (`users-service`, `iam-service`, etc.).
- **Despliegue**: Es el artefacto ejecutable final.

## 2. Reglas Arquitectónicas

- **Dependencias**: Puede depender de TODOS los módulos de la aplicación.
- **Lógica de Negocio**: PROHIBIDO contener lógica de dominio o casos de uso. Solo configuración.
- **Controladores**: No debe definir controladores REST propios, salvo endpoints de infraestructura (ej. health checks, si no se usa Actuator).

## 3. Configuración

- La configuración debe estar centralizada en `application.yml` (y perfiles específicos).
- Preferir configuración explícita (`@Configuration`) sobre auto-configuración mágica cuando sea posible para mayor claridad.
