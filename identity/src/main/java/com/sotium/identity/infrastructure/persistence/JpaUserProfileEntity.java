package com.sotium.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaUserProfileEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio")
    private String bio;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public static JpaUserProfileEntity fromDomain(
        final UUID id,
        final UUID userId,
        final String firstName,
        final String lastName,
        final String phone,
        final String avatarUrl,
        final String bio,
        final OffsetDateTime createdAt,
        final OffsetDateTime updatedAt
    ) {
        final JpaUserProfileEntity entity = new JpaUserProfileEntity();
        entity.id = id;
        entity.userId = userId;
        entity.firstName = firstName;
        entity.lastName = lastName;
        entity.phone = phone;
        entity.avatarUrl = avatarUrl;
        entity.bio = bio;
        entity.createdAt = createdAt;
        entity.updatedAt = updatedAt;
        return entity;
    }
}
