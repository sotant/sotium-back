package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.MembershipAlreadyExistsException;
import com.sotium.identity.application.port.in.AssignOwnerMembershipCommand;
import com.sotium.identity.application.port.in.AssignOwnerMembershipResult;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignOwnerMembershipServiceTest {

    @Test
    @DisplayName("assign_shouldCreateOwnerMembership_whenMissing")
    void assign_shouldCreateOwnerMembership_whenMissing() {
        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);

        final AssignOwnerMembershipResult result = service.assign(
            new AssignOwnerMembershipCommand(UUID.randomUUID(), UUID.randomUUID())
        );

        assertFalse(result.alreadyExisted());
        assertEquals(1, repository.memberships.size());
        assertEquals(MembershipRole.OWNER, repository.memberships.getFirst().role());
        assertEquals(MembershipStatus.ACTIVE, repository.memberships.getFirst().status());
    }

    @Test
    @DisplayName("assign_shouldReturnAlreadyExisted_whenMembershipExists")
    void assign_shouldReturnAlreadyExisted_whenMembershipExists() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        final AcademyMembership existingMembership = new AcademyMembership(
            UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE
        );

        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository(existingMembership);
        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);

        final AssignOwnerMembershipResult result = service.assign(new AssignOwnerMembershipCommand(userId, academyId));

        assertTrue(result.alreadyExisted());
        assertEquals(existingMembership.id(), result.membershipId());
    }

    @Test
    @DisplayName("assign_shouldReturnAlreadyExisted_whenUniqueRaceHappens")
    void assign_shouldReturnAlreadyExisted_whenUniqueRaceHappens() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        final AcademyMembership existingMembership = new AcademyMembership(
            UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE
        );

        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository(existingMembership);
        repository.simulateUniqueRace = true;

        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);

        final AssignOwnerMembershipResult result = service.assign(new AssignOwnerMembershipCommand(userId, academyId));

        assertTrue(result.alreadyExisted());
        assertEquals(existingMembership.id(), result.membershipId());
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {

        private final List<AcademyMembership> memberships = new ArrayList<>();
        private boolean simulateUniqueRace;

        private InMemoryMembershipRepository(final AcademyMembership... seedMemberships) {
            memberships.addAll(List.of(seedMemberships));
        }

        @Override
        public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
            return memberships;
        }

        @Override
        public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
            return memberships.stream()
                .filter(membership -> membership.academyId().equals(academyId) && membership.userId().equals(userId))
                .findFirst();
        }

        @Override
        public AcademyMembership save(final AcademyMembership academyMembership) {
            if (simulateUniqueRace) {
                throw new MembershipAlreadyExistsException("race", new RuntimeException("duplicate"));
            }

            memberships.add(academyMembership);
            return academyMembership;
        }
    }
}
