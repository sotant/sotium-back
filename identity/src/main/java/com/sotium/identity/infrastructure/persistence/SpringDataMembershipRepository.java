package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMembershipRepository extends JpaRepository<JpaMembershipEntity, UUID> {

    List<JpaMembershipEntity> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    List<JpaMembershipEntity> findByUserId(UUID userId);

    List<JpaMembershipEntity> findByAcademyId(UUID academyId);

    Optional<JpaMembershipEntity> findByAcademyIdAndUserId(UUID academyId, UUID userId);

    void deleteByUserId(UUID userId);
}
