package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.exception.DuplicateAcademyMembershipException;
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
    public List<AcademyMembership> findByUserId(final UUID userId) {
        return springDataMembershipRepository.findByUserId(userId)
            .stream()
            .map(PersistenceMappers::toDomain)
            .toList();
    }

    @Override
    public List<AcademyMembership> findByAcademyId(final UUID academyId) {
        return springDataMembershipRepository.findByAcademyId(academyId)
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
            return PersistenceMappers.toDomain(
                springDataMembershipRepository.save(PersistenceMappers.toEntity(academyMembership))
            );
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateAcademyMembershipException(
                academyMembership.academyId(),
                academyMembership.userId(),
                exception
            );
        }
    }

    @Override
    public void deleteByUserId(final UUID userId) {
        springDataMembershipRepository.deleteByUserId(userId);
    }
}
