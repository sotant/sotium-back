package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdentityUserRepositoryAdapter implements IdentityUserRepository {

    private final SpringDataIdentityUserRepository springDataIdentityUserRepository;

    @Override
    public Optional<IdentityUser> findByKeycloakSub(final String keycloakSub) {
        return springDataIdentityUserRepository.findByKeycloakSub(keycloakSub).map(PersistenceMappers::toDomain);
    }
}
