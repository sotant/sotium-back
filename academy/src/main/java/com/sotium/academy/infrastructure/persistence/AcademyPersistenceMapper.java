package com.sotium.academy.infrastructure.persistence;

import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.valueobject.AcademyEmail;
import com.sotium.academy.domain.valueobject.AcademyId;
import com.sotium.academy.domain.valueobject.AcademyName;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class AcademyPersistenceMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AcademyPersistenceMapper() {
    }

    public static AcademyJpaEntity toJpaEntity(final Academy academy) {
        return AcademyJpaEntity.builder()
            .id(academy.id().value())
            .name(academy.name().value())
            .email(academy.email().value())
            .status(academy.status())
            .createdAt(academy.createdAt())
            .build();
    }

    public static Academy toDomain(final AcademyJpaEntity academyJpaEntity) {
        return new Academy(
            new AcademyId(academyJpaEntity.getId()),
            new AcademyName(academyJpaEntity.getName()),
            new AcademyEmail(academyJpaEntity.getEmail()),
            academyJpaEntity.getStatus(),
            academyJpaEntity.getCreatedAt()
        );
    }

    public static AcademySettingsJpaEntity toJpaEntity(final AcademySettings academySettings) {
        return AcademySettingsJpaEntity.builder()
            .academyId(academySettings.academyId().value())
            .phone(academySettings.phone())
            .timezone(academySettings.timezone())
            .openingHours(readTree(academySettings.openingHours()))
            .holidays(readTree(academySettings.holidays()))
            .updatedAt(academySettings.updatedAt())
            .build();
    }

    public static AcademySettings toDomain(final AcademySettingsJpaEntity academySettingsJpaEntity) {
        return new AcademySettings(
            new AcademyId(academySettingsJpaEntity.getAcademyId()),
            academySettingsJpaEntity.getPhone(),
            academySettingsJpaEntity.getTimezone(),
            academySettingsJpaEntity.getOpeningHours().toString(),
            academySettingsJpaEntity.getHolidays().toString(),
            academySettingsJpaEntity.getUpdatedAt()
        );
    }

    private static JsonNode readTree(final String value) {
        try {
            return OBJECT_MAPPER.readTree(value);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid JSON payload for academy settings", exception);
        }
    }
}
