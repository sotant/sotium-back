package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
