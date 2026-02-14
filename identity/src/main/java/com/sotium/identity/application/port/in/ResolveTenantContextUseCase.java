package com.sotium.identity.application.port.in;

import java.util.UUID;

public interface ResolveTenantContextUseCase {

    UUID resolveAcademyId(String keycloakSub, UUID selectedAcademyId);
}
