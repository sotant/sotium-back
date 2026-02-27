package com.sotium.academy.infrastructure.config;

import com.sotium.academy.application.port.in.CreateAcademyUseCase;
import com.sotium.academy.application.port.out.AcademyRepository;
import com.sotium.academy.application.usecase.CreateAcademyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcademyModuleConfiguration {

    @Bean
    public CreateAcademyUseCase createAcademyUseCase(final AcademyRepository academyRepository) {
        return new CreateAcademyService(academyRepository);
    }
}
