package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;

import java.time.Instant;

public final class PersistenceMappers {

    private PersistenceMappers() {
    }

    public static IdentityUser toDomain(final JpaIdentityUserEntity entity) {
        return new IdentityUser(entity.getId(), entity.getKeycloakSub(), entity.getEmail(), entity.getStatus());
    }

    public static AcademyMembership toDomain(final JpaMembershipEntity entity) {
        return new AcademyMembership(entity.getId(), entity.getAcademyId(), entity.getUserId(), entity.getRole(), entity.getStatus());
    }

    public static JpaIdentityUserEntity toJpaEntity(final IdentityUser identityUser, final Instant createdAt) {
        return JpaIdentityUserEntity.builder()
            .id(identityUser.id())
            .keycloakSub(identityUser.keycloakSub())
            .email(identityUser.email())
            .status(identityUser.status())
            .createdAt(createdAt)
            .updatedAt(Instant.now())
            .build();
    }

    public static JpaMembershipEntity toJpaEntity(final AcademyMembership academyMembership) {
        return JpaMembershipEntity.builder()
            .id(academyMembership.id())
            .academyId(academyMembership.academyId())
            .userId(academyMembership.userId())
            .role(academyMembership.role())
            .status(academyMembership.status())
            .createdAt(Instant.now())
            .build();
    }
}
