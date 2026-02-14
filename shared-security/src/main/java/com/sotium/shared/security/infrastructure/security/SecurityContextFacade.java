package com.sotium.shared.security.infrastructure.security;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextFacade {

    public AuthenticatedUser getRequiredAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authenticated user is required");
        }

        if (authentication.getDetails() instanceof AuthenticatedUser user) {
            return user;
        }

        throw new ForbiddenException("Authenticated user details are not available");
    }
}
