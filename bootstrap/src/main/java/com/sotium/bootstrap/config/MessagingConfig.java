package com.sotium.bootstrap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class MessagingConfig {
    // Habilita el procesamiento asíncrono para eventos de aplicación (@EventListener + @Async).
    // Esto prepara el terreno para desacoplar módulos mediante eventos internos,
    // un paso previo a la extracción a mensajería externa (RabbitMQ/Kafka).
}
