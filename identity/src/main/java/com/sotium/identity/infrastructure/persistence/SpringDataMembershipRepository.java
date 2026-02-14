package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataMembershipRepository extends JpaRepository<JpaMembershipEntity, UUID> {

    List<JpaMembershipEntity> findByUserIdAndStatus(UUID userId, MembershipStatus status);
}
