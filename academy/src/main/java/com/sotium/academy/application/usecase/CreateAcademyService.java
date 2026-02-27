package com.sotium.academy.application.usecase;

import com.sotium.academy.application.port.in.CreateAcademyUseCase;
import com.sotium.academy.application.port.out.AcademyRepository;
import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.valueobject.AcademyId;

import java.util.UUID;

public class CreateAcademyService implements CreateAcademyUseCase {

    private final AcademyRepository academyRepository;

    public CreateAcademyService(final AcademyRepository academyRepository) {
        this.academyRepository = academyRepository;
    }

    @Override
    public Academy create(final String name) {
        final Academy academy = new Academy(new AcademyId(UUID.randomUUID()), name, true);
        return academyRepository.save(academy);
    }
}
