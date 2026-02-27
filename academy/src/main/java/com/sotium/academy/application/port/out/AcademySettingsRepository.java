package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.valueobject.AcademyId;

import java.util.Optional;

public interface AcademySettingsRepository {

    AcademySettings save(AcademySettings academySettings);

    Optional<AcademySettings> findByAcademyId(AcademyId academyId);
}
