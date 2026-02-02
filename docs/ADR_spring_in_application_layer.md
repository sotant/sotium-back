# ADR. Uso de anotaciones Spring en la capa Application

- **Fecha** 2026-02-02
- **Estado** Aceptado
- **Autores** Daniel Soto

---

## Contexto

Este proyecto adopta una **Arquitectura Hexagonal + Clean Architecture**, donde las capas internas (Domain y Application) no deberían depender de frameworks externos.

Al utilizar **Spring Boot** como framework principal, surge la necesidad de decidir si la capa **Application (casos de uso)** puede utilizar anotaciones de Spring como:

- `@Service`
- `@Transactional`

Estas anotaciones pertenecen a `org.springframework.*`, lo que introduce una dependencia directa con el framework y, en sentido estricto, viola el principio de inversión de dependencias (DIP).

### Problema a resolver

- Mantener una arquitectura limpia y coherente
- Evitar complejidad innecesaria
- Garantizar testabilidad y mantenibilidad
- Integrar correctamente la gestión transaccional

### Restricciones

- Proyecto basado en Spring Boot
- Prioridad en simplicidad y claridad arquitectónica
- No se busca independencia total del framework a corto plazo

---

## Decisión

Se **permite el uso limitado de anotaciones Spring en la capa Application**, concretamente:

- `@Service` -> es un *stereotype*, no aporta comportamiento técnico
- `@Transactional` -> es un **cross-cutting concern**

### Criterios clave de la decisión

- Estas anotaciones no introducen lógica de infraestructura
- Son mecanismos de orquestación y concerns transversales
- No afectan a la lógica de negocio
- Mantienen la testabilidad de los casos de uso sin levantar el contexto de Spring
- Reducen la complejidad estructural del proyecto

### Regla de oro

> **La capa Application puede conocer Spring, pero Spring no debe gobernar la lógica de negocio.**

---

## Consecuencias

### Positivas

- Menor complejidad y menos clases de configuración
- Uso idiomático de Spring Boot
- Gestión transaccional clara y centralizada
- Casos de uso fáciles de localizar y entender

### Negativas

- Dependencia explícita de Spring en Application
- Menor pureza teórica respecto a Clean Architecture
- Ligero acoplamiento al framework

### Impacto en el equipo/proyecto

- No se requieren cambios en los procesos actuales
- No se necesita formación adicional
- La decisión queda documentada y consensuada

---

## Alternativas consideradas

### Alternativa: Application completamente independiente de Spring

**Descripción**  
Eliminar todas las anotaciones Spring de la capa Application y definir los beans y transacciones en clases `@Configuration` dentro de Infrastructure.

Ejemplo:

```java
@Configuration
public class UserServiceConfig {

    @Bean
    @Transactional
    public UserUseCase userUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return new UserService(userRepository, passwordEncoder);
    }
}
```

**Pros**
- Máxima pureza arquitectónica
- Application 100% independiente del framework
- Fácil reutilización fuera de Spring

**Contras**
- Aumento significativo de clases y configuración
- Mayor complejidad conceptual
- Poco beneficio práctico en el contexto actual

---

## Revisión futura

Esta decisión deberá revisarse si:

- El proyecto requiere desacoplarse de Spring
- Se extraen los casos de uso a un módulo o librería independiente
- Se adopta un enfoque de arquitectura más estrictamente académico

En ese caso, se evaluará migrar a la alternativa sin impacto significativo en el dominio.
