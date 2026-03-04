package com.sotium.identity.application.port.in;

import java.util.UUID;

public interface AssignOwnerMembershipUseCase {

    AssignOwnerMembershipResult assign(AssignOwnerMembershipCommand command);

    record AssignOwnerMembershipCommand(UUID userId, UUID academyId) {
    }

    record AssignOwnerMembershipResult(UUID membershipId, boolean alreadyExisted) {
    }
}
