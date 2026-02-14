package com.sotium.identity.application.port.out;

import com.sotium.identity.domain.model.IdentityUser;

import java.util.Optional;

public interface IdentityUserRepository {

    Optional<IdentityUser> findByKeycloakSub(String keycloakSub);
}
