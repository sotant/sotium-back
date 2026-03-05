package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.Academy;

import java.util.UUID;

public interface AcademyRepositoryPort {

    Academy save(Academy academy);

    void deleteById(UUID academyId);
}
