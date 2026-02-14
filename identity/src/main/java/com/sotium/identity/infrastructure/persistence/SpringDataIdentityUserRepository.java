package com.sotium.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataIdentityUserRepository extends JpaRepository<JpaIdentityUserEntity, UUID> {

    Optional<JpaIdentityUserEntity> findByKeycloakSub(String keycloakSub);
}
