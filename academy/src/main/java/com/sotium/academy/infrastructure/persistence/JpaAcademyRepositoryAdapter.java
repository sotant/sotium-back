package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.domain.model.Academy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaAcademyRepositoryAdapter implements AcademyRepositoryPort {

    private final SpringDataAcademyRepository springDataAcademyRepository;

    @Override
    public Academy save(final Academy academy) {
        final AcademyJpaEntity persistedEntity = springDataAcademyRepository.save(AcademyPersistenceMapper.toJpaEntity(academy));
        return AcademyPersistenceMapper.toDomain(persistedEntity);
    }
}
