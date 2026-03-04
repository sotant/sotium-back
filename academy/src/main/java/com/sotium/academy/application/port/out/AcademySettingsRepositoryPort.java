package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.AcademySettings;

public interface AcademySettingsRepositoryPort {

    AcademySettings save(AcademySettings academySettings);
}
