package com.sotium.academy.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "academy_settings")
public class JpaAcademySettingsEntity {

    @Id
    @Column(name = "academy_id")
    private UUID academyId;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private String locale;

    protected JpaAcademySettingsEntity() {
    }

    public JpaAcademySettingsEntity(final UUID academyId, final String timezone, final String locale) {
        this.academyId = academyId;
        this.timezone = timezone;
        this.locale = locale;
    }

    public UUID getAcademyId() {
        return academyId;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLocale() {
        return locale;
    }
}
