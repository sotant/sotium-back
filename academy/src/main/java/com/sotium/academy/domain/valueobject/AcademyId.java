package com.sotium.academy.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record AcademyId(UUID value) {

    public AcademyId {
        Objects.requireNonNull(value, "academyId cannot be null");
    }

    public static AcademyId newId() {
        return new AcademyId(UUID.randomUUID());
    }
}
