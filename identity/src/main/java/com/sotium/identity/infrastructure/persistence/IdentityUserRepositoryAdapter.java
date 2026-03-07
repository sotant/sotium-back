package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class IdentityUserRepositoryAdapter implements IdentityUserRepository {

    private final SpringDataIdentityUserRepository springDataIdentityUserRepository;

    @Override
    public Optional<IdentityUser> findById(final UUID identityUserId) {
        return springDataIdentityUserRepository.findById(identityUserId).map(PersistenceMappers::toDomain);
    }

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
        return PersistenceMappers.toDomain(
            springDataIdentityUserRepository.save(PersistenceMappers.toEntity(identityUser))
        );
    }

    @Override
    public void deleteById(final UUID identityUserId) {
        springDataIdentityUserRepository.deleteById(identityUserId);
    }
}
