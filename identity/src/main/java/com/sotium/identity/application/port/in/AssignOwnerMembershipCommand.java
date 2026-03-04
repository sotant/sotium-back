package com.sotium.identity.application.port.in;

import java.util.UUID;

public record AssignOwnerMembershipCommand(
    UUID userId,
    UUID academyId
) {
}
