package com.sotium.identity.application.port.out;

import com.sotium.identity.domain.model.IdentityUser;

import java.util.Optional;
import java.util.UUID;

public interface IdentityUserRepository {

    Optional<IdentityUser> findById(UUID identityUserId);

    Optional<IdentityUser> findByKeycloakSub(String keycloakSub);

    Optional<IdentityUser> findByEmail(String email);

    IdentityUser save(IdentityUser identityUser);

    void deleteById(UUID identityUserId);
}
