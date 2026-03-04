package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.exception.MembershipAlreadyExistsException;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MembershipRepositoryAdapter implements MembershipRepository {

    private final SpringDataMembershipRepository springDataMembershipRepository;

    @Override
    public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
        return springDataMembershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)
            .stream()
            .map(PersistenceMappers::toDomain)
            .toList();
    }

    @Override
    public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
        return springDataMembershipRepository.findByAcademyIdAndUserId(academyId, userId)
            .map(PersistenceMappers::toDomain);
    }

    @Override
    public AcademyMembership save(final AcademyMembership academyMembership) {
        try {
            final JpaMembershipEntity persistedMembership = springDataMembershipRepository.save(
                PersistenceMappers.toJpaEntity(academyMembership)
            );
            return PersistenceMappers.toDomain(persistedMembership);
        } catch (DataIntegrityViolationException exception) {
            throw new MembershipAlreadyExistsException("Membership already exists for academy/user", exception);
        }
    }
}
