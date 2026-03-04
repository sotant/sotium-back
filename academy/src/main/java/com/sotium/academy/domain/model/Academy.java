package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyEmail;
import com.sotium.academy.domain.valueobject.AcademyId;
import com.sotium.academy.domain.valueobject.AcademyName;

import java.time.Instant;
import java.util.Objects;

public record Academy(
    AcademyId id,
    AcademyName name,
    AcademyEmail email,
    AcademyStatus status,
    Instant createdAt
) {

    public Academy {
        Objects.requireNonNull(id, "academyId cannot be null");
        Objects.requireNonNull(name, "academyName cannot be null");
        Objects.requireNonNull(email, "academyEmail cannot be null");
        Objects.requireNonNull(status, "academyStatus cannot be null");
        Objects.requireNonNull(createdAt, "academyCreatedAt cannot be null");
    }

    public static Academy create(final AcademyName name, final AcademyEmail email) {
        return new Academy(
            AcademyId.newId(),
            name,
            email,
            AcademyStatus.ACTIVE,
            Instant.now()
        );
    }
}
