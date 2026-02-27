package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyId;

public record AcademySettings(
    AcademyId academyId,
    String timezone,
    String locale
) {

    public AcademySettings {
        if (academyId == null) {
            throw new IllegalArgumentException("academy id must not be null");
        }
        if (timezone == null || timezone.isBlank()) {
            throw new IllegalArgumentException("timezone must not be blank");
        }
        if (locale == null || locale.isBlank()) {
            throw new IllegalArgumentException("locale must not be blank");
        }
    }
}
