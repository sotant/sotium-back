package com.sotium.academy.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "academy_settings")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AcademySettingsJpaEntity {

    @Id
    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours", nullable = false, columnDefinition = "jsonb")
    private JsonNode openingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "holidays", nullable = false, columnDefinition = "jsonb")
    private JsonNode holidays;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
