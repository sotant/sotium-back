package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.application.port.out.AcademySettingsRepository;
import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.valueobject.AcademyId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AcademySettingsRepositoryAdapter implements AcademySettingsRepository {

    private final SpringDataAcademySettingsRepository springDataAcademySettingsRepository;

    public AcademySettingsRepositoryAdapter(final SpringDataAcademySettingsRepository springDataAcademySettingsRepository) {
        this.springDataAcademySettingsRepository = springDataAcademySettingsRepository;
    }

    @Override
    public AcademySettings save(final AcademySettings academySettings) {
        final JpaAcademySettingsEntity entity = AcademyPersistenceMapper.toEntity(academySettings);
        final JpaAcademySettingsEntity savedEntity = springDataAcademySettingsRepository.save(entity);
        return AcademyPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<AcademySettings> findByAcademyId(final AcademyId academyId) {
        return springDataAcademySettingsRepository.findById(academyId.value()).map(AcademyPersistenceMapper::toDomain);
    }
}
