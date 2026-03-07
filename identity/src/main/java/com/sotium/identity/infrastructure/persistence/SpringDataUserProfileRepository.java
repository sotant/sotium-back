package com.sotium.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserProfileRepository extends JpaRepository<JpaUserProfileEntity, UUID> {

    Optional<JpaUserProfileEntity> findByUserId(UUID userId);
}
