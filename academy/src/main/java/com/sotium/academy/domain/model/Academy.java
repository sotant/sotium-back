package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyId;

public record Academy(
    AcademyId id,
    String name,
    boolean active
) {

    public Academy {
        if (id == null) {
            throw new IllegalArgumentException("academy id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("academy name must not be blank");
        }
    }
}
