package com.sotium.identity.application.exception;

import java.util.UUID;

public class DuplicateAcademyMembershipException extends RuntimeException {

    public DuplicateAcademyMembershipException(final UUID academyId, final UUID userId, final Throwable cause) {
        super("Duplicate membership for academyId=%s userId=%s".formatted(academyId, userId), cause);
    }
}
