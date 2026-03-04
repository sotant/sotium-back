package com.sotium.identity.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ResolveTenantContextUseCase {

    UUID resolveAcademyId(String keycloakSub, UUID selectedAcademyId);

    List<UUID> resolveAccessibleAcademyIds(String keycloakSub);

    boolean hasAccessToAcademy(String keycloakSub, UUID academyId);
}
