package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.DuplicateAcademyMembershipException;
import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase.AssignOwnerMembershipCommand;
import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase.AssignOwnerMembershipResult;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignOwnerMembershipServiceTest {

    @Test
    @DisplayName("assign_shouldCreateMembership_whenNotExists")
    void assign_shouldCreateMembership_whenNotExists() {
        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository(false);
        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final AssignOwnerMembershipResult result = service.assign(new AssignOwnerMembershipCommand(userId, academyId));

        assertFalse(result.alreadyExisted());
        final AcademyMembership membership = repository.findByAcademyIdAndUserId(academyId, userId).orElseThrow();
        assertEquals(MembershipRole.OWNER, membership.role());
        assertEquals(MembershipStatus.ACTIVE, membership.status());
    }

    @Test
    @DisplayName("assign_shouldNotCreateMembership_whenAlreadyExists")
    void assign_shouldNotCreateMembership_whenAlreadyExists() {
        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository(false);
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        final AcademyMembership existingMembership = new AcademyMembership(
            UUID.randomUUID(),
            academyId,
            userId,
            MembershipRole.OWNER,
            MembershipStatus.ACTIVE
        );
        repository.save(existingMembership);

        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);
        final AssignOwnerMembershipResult result = service.assign(new AssignOwnerMembershipCommand(userId, academyId));

        assertTrue(result.alreadyExisted());
        assertEquals(existingMembership.id(), result.membershipId());
    }

    @Test
    @DisplayName("assign_shouldTreatUniqueCollisionAsIdempotent")
    void assign_shouldTreatUniqueCollisionAsIdempotent() {
        final InMemoryMembershipRepository repository = new InMemoryMembershipRepository(true);
        final AssignOwnerMembershipService service = new AssignOwnerMembershipService(repository);
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final AssignOwnerMembershipResult result = service.assign(new AssignOwnerMembershipCommand(userId, academyId));

        assertTrue(result.alreadyExisted());
        assertEquals(
            repository.findByAcademyIdAndUserId(academyId, userId).orElseThrow().id(),
            result.membershipId()
        );
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {

        private final Map<String, AcademyMembership> memberships = new HashMap<>();
        private final boolean forceDuplicateOnFirstSave;
        private boolean duplicateTriggered;

        private InMemoryMembershipRepository(final boolean forceDuplicateOnFirstSave) {
            this.forceDuplicateOnFirstSave = forceDuplicateOnFirstSave;
        }

        @Override
        public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId))
                .filter(membership -> membership.status() == MembershipStatus.ACTIVE)
                .toList();
        }

        @Override
        public List<AcademyMembership> findByUserId(final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId))
                .toList();
        }

        @Override
        public List<AcademyMembership> findByAcademyId(final UUID academyId) {
            return memberships.values().stream()
                .filter(membership -> membership.academyId().equals(academyId))
                .toList();
        }

        @Override
        public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
            return Optional.ofNullable(memberships.get(key(academyId, userId)));
        }

        @Override
        public AcademyMembership save(final AcademyMembership academyMembership) {
            final String key = key(academyMembership.academyId(), academyMembership.userId());
            if (forceDuplicateOnFirstSave && !duplicateTriggered) {
                duplicateTriggered = true;
                final AcademyMembership existingMembership = new AcademyMembership(
                    UUID.randomUUID(),
                    academyMembership.academyId(),
                    academyMembership.userId(),
                    academyMembership.role(),
                    academyMembership.status()
                );
                memberships.put(key, existingMembership);
                throw new DuplicateAcademyMembershipException(
                    academyMembership.academyId(),
                    academyMembership.userId(),
                    new RuntimeException("simulated unique violation")
                );
            }
            memberships.put(key, academyMembership);
            return academyMembership;
        }

        @Override
        public void deleteByUserId(final UUID userId) {
            memberships.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
        }

        private String key(final UUID academyId, final UUID userId) {
            return academyId + ":" + userId;
        }
    }
}
