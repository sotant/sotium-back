package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.valueobject.AcademyId;

import java.util.Optional;

public interface AcademyRepository {

    Academy save(Academy academy);

    Optional<Academy> findById(AcademyId academyId);
}
