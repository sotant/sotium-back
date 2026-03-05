package com.sotium.identity.application.usecase;

import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteIdentityBySubService implements DeleteIdentityBySubUseCase {

    private final IdentityUserRepository identityUserRepository;
    private final MembershipRepository membershipRepository;
    private final AcademySettingsRepositoryPort academySettingsRepositoryPort;
    private final AcademyRepositoryPort academyRepositoryPort;

    @Override
    @Transactional
    public DeleteIdentityBySubResult delete(final DeleteIdentityBySubCommand command) {
        Objects.requireNonNull(command, "deleteIdentityBySubCommand must not be null");
        final String sub = Objects.requireNonNull(command.sub(), "sub must not be null").trim();
        if (sub.isBlank()) {
            throw new IllegalArgumentException("sub must not be blank");
        }

        final var identityUserOptional = identityUserRepository.findByKeycloakSub(sub);
        if (identityUserOptional.isEmpty()) {
            return new DeleteIdentityBySubResult(false);
        }

        final UUID userId = identityUserOptional.orElseThrow().id();
        final Set<UUID> academyIds = membershipRepository.findByUserId(userId)
            .stream()
            .map(membership -> membership.academyId())
            .collect(java.util.stream.Collectors.toSet());

        membershipRepository.deleteByUserId(userId);
        identityUserRepository.deleteById(userId);

        for (UUID academyId : academyIds) {
            academySettingsRepositoryPort.deleteByAcademyId(academyId);
            academyRepositoryPort.deleteById(academyId);
        }

        return new DeleteIdentityBySubResult(true);
    }
}
