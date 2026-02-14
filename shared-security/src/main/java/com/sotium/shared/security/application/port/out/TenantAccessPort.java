package com.sotium.shared.security.application.port.out;

import com.sotium.shared.security.domain.model.AuthenticatedUser;

import java.util.UUID;

public interface TenantAccessPort {

    UUID resolveAcademyId(AuthenticatedUser authenticatedUser, UUID selectedAcademyId);
}
