package com.sotium.identity.infrastructure.security;

import com.sotium.identity.application.exception.IdentityAccessDeniedException;
import com.sotium.identity.application.port.in.ResolveTenantContextUseCase;
import com.sotium.shared.security.application.port.out.TenantAccessPort;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAccessPortAdapter implements TenantAccessPort {

    private final ResolveTenantContextUseCase resolveTenantContextUseCase;

    @Override
    public UUID resolveAcademyId(final AuthenticatedUser authenticatedUser, final UUID selectedAcademyId) {
        try {
            return resolveTenantContextUseCase.resolveAcademyId(authenticatedUser.sub(), selectedAcademyId);
        } catch (IdentityAccessDeniedException exception) {
            log.warn("Tenant access denied for subject={} reason={}", authenticatedUser.sub(), exception.getMessage());
            throw new ForbiddenException(exception.getMessage());
        }
    }
}
