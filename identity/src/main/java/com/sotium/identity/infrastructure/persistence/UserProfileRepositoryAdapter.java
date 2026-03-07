package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.port.out.UserProfileRepository;
import com.sotium.identity.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileRepository springDataUserProfileRepository;

    @Override
    public Optional<UserProfile> findByUserId(final UUID userId) {
        return springDataUserProfileRepository.findByUserId(userId).map(PersistenceMappers::toDomain);
    }

    @Override
    public UserProfile save(final UserProfile userProfile) {
        return PersistenceMappers.toDomain(
            springDataUserProfileRepository.save(PersistenceMappers.toEntity(userProfile))
        );
    }
}
