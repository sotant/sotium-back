package com.sotium.identity.application.port.out;

import com.sotium.identity.domain.model.AcademyMembership;

import java.util.List;
import java.util.UUID;

public interface MembershipRepository {

    List<AcademyMembership> findActiveMembershipsByUserId(UUID userId);
}
