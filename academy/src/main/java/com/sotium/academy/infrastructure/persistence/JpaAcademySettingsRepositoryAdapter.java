package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.academy.domain.model.AcademySettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaAcademySettingsRepositoryAdapter implements AcademySettingsRepositoryPort {

    private final SpringDataAcademySettingsRepository springDataAcademySettingsRepository;

    @Override
    public AcademySettings save(final AcademySettings academySettings) {
        final AcademySettingsJpaEntity persistedEntity = springDataAcademySettingsRepository.save(
            AcademyPersistenceMapper.toJpaEntity(academySettings)
        );
        return AcademyPersistenceMapper.toDomain(persistedEntity);
    }
}
