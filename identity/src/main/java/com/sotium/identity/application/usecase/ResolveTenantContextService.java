package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityAccessDeniedException;
import com.sotium.identity.application.port.in.ResolveTenantContextUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves active academy context using strict identity and membership checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResolveTenantContextService implements ResolveTenantContextUseCase {

    private final IdentityUserRepository identityUserRepository;
    private final MembershipRepository membershipRepository;

    @Override
    public UUID resolveAcademyId(final String keycloakSub, final UUID selectedAcademyId) {
        final UUID userId = identityUserRepository.findByKeycloakSub(keycloakSub)
            .filter(user -> user.status() == IdentityUserStatus.ACTIVE)
            .orElseThrow(() -> {
                log.warn("Identity user not provisioned or inactive for subject={}", keycloakSub);
                return new IdentityAccessDeniedException("Authenticated user is not provisioned in identity context");
            })
            .id();

        final List<AcademyMembership> activeMemberships = membershipRepository.findActiveMembershipsByUserId(userId);
        if (activeMemberships.isEmpty()) {
            log.warn("No active memberships for subject={} userId={}", keycloakSub, userId);
            throw new IdentityAccessDeniedException("User does not have active academy memberships");
        }

        if (selectedAcademyId != null) {
            return activeMemberships.stream()
                .filter(membership -> membership.academyId().equals(selectedAcademyId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Selected academy {} not owned by subject={} userId={}", selectedAcademyId, keycloakSub, userId);
                    return new IdentityAccessDeniedException("Selected academy does not belong to the authenticated user");
                })
                .academyId();
        }

        if (activeMemberships.size() == 1) {
            return activeMemberships.getFirst().academyId();
        }

        log.warn("Multiple active memberships found for subject={} userId={}, explicit academy selection required", keycloakSub, userId);
        throw new IdentityAccessDeniedException("Multiple memberships found; explicit academy selection is required");
    }
}
