package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.UserProfile;

public final class PersistenceMappers {

    private PersistenceMappers() {
    }

    public static IdentityUser toDomain(final JpaIdentityUserEntity entity) {
        return new IdentityUser(entity.getId(), entity.getKeycloakSub(), entity.getEmail(), entity.getStatus());
    }

    public static AcademyMembership toDomain(final JpaMembershipEntity entity) {
        return new AcademyMembership(entity.getId(), entity.getAcademyId(), entity.getUserId(), entity.getRole(), entity.getStatus());
    }

    public static JpaIdentityUserEntity toEntity(final IdentityUser identityUser) {
        return JpaIdentityUserEntity.fromDomain(
            identityUser.id(),
            identityUser.keycloakSub(),
            identityUser.email(),
            identityUser.status()
        );
    }

    public static UserProfile toDomain(final JpaUserProfileEntity entity) {
        return new UserProfile(
            entity.getId(),
            entity.getUserId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getPhone(),
            entity.getAvatarUrl(),
            entity.getBio(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static JpaUserProfileEntity toEntity(final UserProfile userProfile) {
        return JpaUserProfileEntity.fromDomain(
            userProfile.id(),
            userProfile.userId(),
            userProfile.firstName(),
            userProfile.lastName(),
            userProfile.phone(),
            userProfile.avatarUrl(),
            userProfile.bio(),
            userProfile.createdAt(),
            userProfile.updatedAt()
        );
    }

    public static JpaMembershipEntity toEntity(final AcademyMembership academyMembership) {
        return JpaMembershipEntity.fromDomain(
            academyMembership.id(),
            academyMembership.academyId(),
            academyMembership.userId(),
            academyMembership.role(),
            academyMembership.status()
        );
    }
}
