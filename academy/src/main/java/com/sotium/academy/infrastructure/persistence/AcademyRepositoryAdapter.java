package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.application.port.out.AcademyRepository;
import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.valueobject.AcademyId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AcademyRepositoryAdapter implements AcademyRepository {

    private final SpringDataAcademyRepository springDataAcademyRepository;

    public AcademyRepositoryAdapter(final SpringDataAcademyRepository springDataAcademyRepository) {
        this.springDataAcademyRepository = springDataAcademyRepository;
    }

    @Override
    public Academy save(final Academy academy) {
        final JpaAcademyEntity entity = AcademyPersistenceMapper.toEntity(academy);
        final JpaAcademyEntity savedEntity = springDataAcademyRepository.save(entity);
        return AcademyPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Academy> findById(final AcademyId academyId) {
        return springDataAcademyRepository.findById(academyId.value()).map(AcademyPersistenceMapper::toDomain);
    }
}
