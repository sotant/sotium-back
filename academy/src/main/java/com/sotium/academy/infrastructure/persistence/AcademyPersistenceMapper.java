package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.valueobject.AcademyId;

public final class AcademyPersistenceMapper {

    private AcademyPersistenceMapper() {
    }

    public static Academy toDomain(final JpaAcademyEntity entity) {
        return new Academy(new AcademyId(entity.getId()), entity.getName(), entity.isActive());
    }

    public static JpaAcademyEntity toEntity(final Academy academy) {
        return new JpaAcademyEntity(academy.id().value(), academy.name(), academy.active());
    }

    public static AcademySettings toDomain(final JpaAcademySettingsEntity entity) {
        return new AcademySettings(new AcademyId(entity.getAcademyId()), entity.getTimezone(), entity.getLocale());
    }

    public static JpaAcademySettingsEntity toEntity(final AcademySettings settings) {
        return new JpaAcademySettingsEntity(
            settings.academyId().value(),
            settings.timezone(),
            settings.locale()
        );
    }
}
