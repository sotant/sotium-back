package com.sotium.identity.application.port.out;

import com.sotium.identity.domain.model.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {

    Optional<UserProfile> findByUserId(UUID userId);

    UserProfile save(UserProfile userProfile);
}
