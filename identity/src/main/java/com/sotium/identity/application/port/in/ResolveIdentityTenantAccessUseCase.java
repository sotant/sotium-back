package com.sotium.identity.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ResolveIdentityTenantAccessUseCase {

    List<UUID> resolveAccessibleAcademyIds(String keycloakSub);

    boolean hasAccessToAcademy(String keycloakSub, UUID academyId);
}
