package com.sotium.shared.security.infrastructure.security;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityContextFacadeTest {

    private final SecurityContextFacade facade = new SecurityContextFacade();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("securityContextFacade_shouldReturnAuthenticatedUser_whenDetailsTypeMatches")
    void securityContextFacade_shouldReturnAuthenticatedUser_whenDetailsTypeMatches() {
        final AuthenticatedUser expectedUser = new AuthenticatedUser("sub-1", "user@test.com", Set.of(Role.ADMIN));
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken("principal", "credentials");
        authentication.setAuthenticated(true);
        authentication.setDetails(expectedUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final AuthenticatedUser user = facade.getRequiredAuthenticatedUser();

        assertSame(expectedUser, user);
    }

    @Test
    @DisplayName("securityContextFacade_shouldThrowForbidden_whenAuthenticationMissingOrNotAuthenticated")
    void securityContextFacade_shouldThrowForbidden_whenAuthenticationMissingOrNotAuthenticated() {
        final ForbiddenException noAuthentication = assertThrows(ForbiddenException.class, facade::getRequiredAuthenticatedUser);
        assertEquals("Authenticated user is required", noAuthentication.getMessage());

        final TestingAuthenticationToken authentication = new TestingAuthenticationToken("principal", "credentials");
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final ForbiddenException notAuthenticated = assertThrows(ForbiddenException.class, facade::getRequiredAuthenticatedUser);
        assertEquals("Authenticated user is required", notAuthenticated.getMessage());
    }

    @Test
    @DisplayName("securityContextFacade_shouldThrowForbidden_whenDetailsIsNotAuthenticatedUser")
    void securityContextFacade_shouldThrowForbidden_whenDetailsIsNotAuthenticatedUser() {
        final TestingAuthenticationToken authentication = new TestingAuthenticationToken("principal", "credentials");
        authentication.setAuthenticated(true);
        authentication.setDetails("not-authenticated-user");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        final ForbiddenException exception = assertThrows(ForbiddenException.class, facade::getRequiredAuthenticatedUser);

        assertEquals("Authenticated user details are not available", exception.getMessage());
    }
}
