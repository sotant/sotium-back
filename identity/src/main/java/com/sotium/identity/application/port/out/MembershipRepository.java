package com.sotium.identity.application.port.out;

import com.sotium.identity.domain.model.AcademyMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository {

    List<AcademyMembership> findActiveMembershipsByUserId(UUID userId);

    List<AcademyMembership> findByUserId(UUID userId);

    Optional<AcademyMembership> findByAcademyIdAndUserId(UUID academyId, UUID userId);

    AcademyMembership save(AcademyMembership academyMembership);

    void deleteByUserId(UUID userId);
}
