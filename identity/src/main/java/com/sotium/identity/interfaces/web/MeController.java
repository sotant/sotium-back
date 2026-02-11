package com.sotium.identity.interfaces.web;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import com.sotium.shared.security.infrastructure.web.filter.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/identity/me")
@RequiredArgsConstructor
public class MeController {

    private final SecurityContextFacade securityContextFacade;
    private final TenantContextHolder tenantContextHolder;

    @GetMapping
    public MeResponse me() {
        final AuthenticatedUser user = securityContextFacade.getRequiredAuthenticatedUser();
        final UUID academyId = tenantContextHolder.get().map(context -> context.academyId()).orElse(null);

        if (!user.isAdmin() && academyId == null) {
            throw new ForbiddenException("Tenant context is required");
        }

        return new MeResponse(user.sub(), user.email(), user.authorities(), academyId);
    }
}
