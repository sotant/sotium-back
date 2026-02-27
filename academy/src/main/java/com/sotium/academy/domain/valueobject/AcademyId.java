package com.sotium.academy.domain.valueobject;

import java.util.UUID;

public record AcademyId(UUID value) {

    public AcademyId {
        if (value == null) {
            throw new IllegalArgumentException("academy id must not be null");
        }
    }
}
