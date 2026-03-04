package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdentityUserRepositoryAdapter implements IdentityUserRepository {

    private final SpringDataIdentityUserRepository springDataIdentityUserRepository;

    @Override
    public Optional<IdentityUser> findByKeycloakSub(final String keycloakSub) {
        return springDataIdentityUserRepository.findByKeycloakSub(keycloakSub).map(PersistenceMappers::toDomain);
    }

    @Override
    public Optional<IdentityUser> findByEmail(final String email) {
        return springDataIdentityUserRepository.findByEmail(email).map(PersistenceMappers::toDomain);
    }

    @Override
    public IdentityUser save(final IdentityUser identityUser) {
        final Instant createdAt = springDataIdentityUserRepository.findById(identityUser.id())
            .map(JpaIdentityUserEntity::getCreatedAt)
            .orElseGet(Instant::now);

        final JpaIdentityUserEntity persistedUser = springDataIdentityUserRepository.save(
            PersistenceMappers.toJpaEntity(identityUser, createdAt)
        );
        return PersistenceMappers.toDomain(persistedUser);
    }
}
