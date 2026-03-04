package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.ResolveIdentityTenantAccessUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResolveIdentityTenantAccessService implements ResolveIdentityTenantAccessUseCase {

    private final IdentityUserRepository identityUserRepository;
    private final MembershipRepository membershipRepository;

    @Override
    public List<UUID> resolveAccessibleAcademyIds(final String keycloakSub) {
        final IdentityUser user = resolveActiveUser(keycloakSub);
        if (user == null) {
            return List.of();
        }

        return membershipRepository.findActiveMembershipsByUserId(user.id())
            .stream()
            .map(AcademyMembership::academyId)
            .toList();
    }

    @Override
    public boolean hasAccessToAcademy(final String keycloakSub, final UUID academyId) {
        Objects.requireNonNull(academyId, "academyId cannot be null");
        return resolveAccessibleAcademyIds(keycloakSub)
            .stream()
            .anyMatch(accessibleAcademyId -> accessibleAcademyId.equals(academyId));
    }

    private IdentityUser resolveActiveUser(final String keycloakSub) {
        if (keycloakSub == null || keycloakSub.isBlank()) {
            return null;
        }

        return identityUserRepository.findByKeycloakSub(keycloakSub)
            .filter(user -> user.status() == IdentityUserStatus.ACTIVE)
            .orElse(null);
    }
}
