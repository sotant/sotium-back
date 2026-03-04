package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import com.sotium.identity.application.exception.DuplicateAcademyMembershipException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignOwnerMembershipService implements AssignOwnerMembershipUseCase {

    private final MembershipRepository membershipRepository;

    @Override
    public AssignOwnerMembershipResult assign(final AssignOwnerMembershipCommand command) {
        final UUID userId = Objects.requireNonNull(command.userId(), "userId must not be null");
        final UUID academyId = Objects.requireNonNull(command.academyId(), "academyId must not be null");

        return membershipRepository.findByAcademyIdAndUserId(academyId, userId)
            .map(existingMembership -> new AssignOwnerMembershipResult(existingMembership.id(), true))
            .orElseGet(() -> createMembership(userId, academyId));
    }

    private AssignOwnerMembershipResult createMembership(final UUID userId, final UUID academyId) {
        final AcademyMembership membershipToCreate = new AcademyMembership(
            UUID.randomUUID(),
            academyId,
            userId,
            MembershipRole.OWNER,
            MembershipStatus.ACTIVE
        );

        try {
            final AcademyMembership createdMembership = membershipRepository.save(membershipToCreate);
            return new AssignOwnerMembershipResult(createdMembership.id(), false);
        } catch (DuplicateAcademyMembershipException ignored) {
            // Check-then-insert is prone to races; treating unique collisions as already-existing
            // keeps retries and concurrent onboarding calls idempotent.
            final AcademyMembership existingMembership = membershipRepository.findByAcademyIdAndUserId(academyId, userId)
                .orElseThrow(() -> ignored);
            return new AssignOwnerMembershipResult(existingMembership.id(), true);
        }
    }
}
