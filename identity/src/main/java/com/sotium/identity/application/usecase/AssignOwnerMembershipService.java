package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.MembershipAlreadyExistsException;
import com.sotium.identity.application.port.in.AssignOwnerMembershipCommand;
import com.sotium.identity.application.port.in.AssignOwnerMembershipResult;
import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
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
        Objects.requireNonNull(command, "assignOwnerMembershipCommand cannot be null");
        Objects.requireNonNull(command.userId(), "userId cannot be null");
        Objects.requireNonNull(command.academyId(), "academyId cannot be null");

        final AcademyMembership existingMembership = membershipRepository
            .findByAcademyIdAndUserId(command.academyId(), command.userId())
            .orElse(null);

        if (existingMembership != null) {
            return new AssignOwnerMembershipResult(existingMembership.id(), true);
        }

        final AcademyMembership ownerMembership = new AcademyMembership(
            UUID.randomUUID(),
            command.academyId(),
            command.userId(),
            MembershipRole.OWNER,
            MembershipStatus.ACTIVE
        );

        try {
            final AcademyMembership persistedMembership = membershipRepository.save(ownerMembership);
            return new AssignOwnerMembershipResult(persistedMembership.id(), false);
        } catch (MembershipAlreadyExistsException exception) {
            // Two concurrent onboarding requests can race between the pre-check and insert.
            // We intentionally convert the unique-constraint collision to a successful idempotent response.
            final AcademyMembership membership = membershipRepository
                .findByAcademyIdAndUserId(command.academyId(), command.userId())
                .orElseThrow(() -> exception);
            return new AssignOwnerMembershipResult(membership.id(), true);
        }
    }
}
