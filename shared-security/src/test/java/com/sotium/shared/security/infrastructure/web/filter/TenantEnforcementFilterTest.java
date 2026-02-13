package com.sotium.shared.security.infrastructure.web.filter;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import com.sotium.shared.security.domain.model.TenantContext;
import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantEnforcementFilterTest {

    @Test
    @DisplayName("tenantEnforcementFilter_shouldThrowForbidden_whenNonAdminWithoutTenantContext")
    void tenantEnforcementFilter_shouldThrowForbidden_whenNonAdminWithoutTenantContext() {
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantEnforcementFilter filter = new TenantEnforcementFilter(tenantContextHolder, securityContextFacade);

        when(securityContextFacade.getRequiredAuthenticatedUser())
            .thenReturn(new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER)));
        when(tenantContextHolder.get()).thenReturn(Optional.empty());

        final ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), mock(FilterChain.class))
        );

        assertEquals("Tenant context is required for this request", exception.getMessage());
    }

    @Test
    @DisplayName("tenantEnforcementFilter_shouldPass_whenAdminWithoutTenantContext")
    void tenantEnforcementFilter_shouldPass_whenAdminWithoutTenantContext() throws ServletException, IOException {
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantEnforcementFilter filter = new TenantEnforcementFilter(tenantContextHolder, securityContextFacade);
        final FilterChain filterChain = mock(FilterChain.class);

        when(securityContextFacade.getRequiredAuthenticatedUser())
            .thenReturn(new AuthenticatedUser("sub-1", "admin@test.com", Set.of(Role.ADMIN)));

        filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("tenantEnforcementFilter_shouldNotFilter_publicAndDocsPaths")
    void tenantEnforcementFilter_shouldNotFilter_publicAndDocsPaths() throws ServletException, IOException {
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantEnforcementFilter filter = new TenantEnforcementFilter(tenantContextHolder, securityContextFacade);
        final FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request("/api/public/identity/academy-registration"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("/actuator/health"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("/swagger-ui/index.html"), new MockHttpServletResponse(), filterChain);
        filter.doFilter(request("/v3/api-docs"), new MockHttpServletResponse(), filterChain);

        verify(securityContextFacade, never()).getRequiredAuthenticatedUser();
        verify(filterChain, times(4)).doFilter(any(), any());
    }

    @Test
    @DisplayName("tenantEnforcementFilter_shouldFilter_nonPublicPath")
    void tenantEnforcementFilter_shouldFilter_nonPublicPath() throws ServletException, IOException {
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantEnforcementFilter filter = new TenantEnforcementFilter(tenantContextHolder, securityContextFacade);

        when(securityContextFacade.getRequiredAuthenticatedUser())
            .thenReturn(new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER)));
        when(tenantContextHolder.get()).thenReturn(Optional.of(new TenantContext(UUID.randomUUID())));

        filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), mock(FilterChain.class));

        verify(securityContextFacade).getRequiredAuthenticatedUser();
    }

    private MockHttpServletRequest request(final String uri) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
