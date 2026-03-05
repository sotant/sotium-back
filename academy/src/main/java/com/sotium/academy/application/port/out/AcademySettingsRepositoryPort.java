package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.AcademySettings;

import java.util.UUID;

public interface AcademySettingsRepositoryPort {

    AcademySettings save(AcademySettings academySettings);

    void deleteByAcademyId(UUID academyId);
}
