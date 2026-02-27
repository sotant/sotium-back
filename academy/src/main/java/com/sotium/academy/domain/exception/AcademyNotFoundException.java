package com.sotium.academy.domain.exception;

import com.sotium.academy.domain.valueobject.AcademyId;

public class AcademyNotFoundException extends RuntimeException {

    public AcademyNotFoundException(final AcademyId academyId) {
        super("academy not found: " + academyId.value());
    }
}
