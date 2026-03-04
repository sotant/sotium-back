package com.sotium.identity.application.port.in;

import java.util.UUID;

public record AssignOwnerMembershipResult(
    UUID membershipId,
    boolean alreadyExisted
) {
}
