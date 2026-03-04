package com.sotium.academy.infrastructure.config;

import com.sotium.academy.application.port.in.AcademyRegistrationPort;
import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.academy.application.usecase.CreateAcademyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcademyModuleConfig {

    @Bean
    AcademyRegistrationPort academyRegistrationPort(
        final AcademyRepositoryPort academyRepositoryPort,
        final AcademySettingsRepositoryPort academySettingsRepositoryPort
    ) {
        return new CreateAcademyService(academyRepositoryPort, academySettingsRepositoryPort);
    }
}
