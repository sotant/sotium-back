package com.sotium.bootstrap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    // La configuración de transacciones se maneja principalmente por Spring Boot y JPA.
    // Esta clase habilita explícitamente la gestión de transacciones basada en anotaciones (@Transactional).
}
