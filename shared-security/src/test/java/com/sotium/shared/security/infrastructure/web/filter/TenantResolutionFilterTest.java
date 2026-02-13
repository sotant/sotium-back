package com.sotium.shared.security.infrastructure.web.filter;

import com.sotium.shared.security.application.port.out.TenantAccessPort;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import com.sotium.shared.security.domain.model.TenantContext;
import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TenantResolutionFilterTest {

    @Test
    @DisplayName("tenantResolutionFilter_shouldResolveAndStoreTenant_whenUserRequiresTenant")
    void tenantResolutionFilter_shouldResolveAndStoreTenant_whenUserRequiresTenant() throws ServletException, IOException {
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantSelection tenantSelection = mock(TenantSelection.class);
        final TenantAccessPort tenantAccessPort = mock(TenantAccessPort.class);
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final FilterChain filterChain = mock(FilterChain.class);

        final AuthenticatedUser user = new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER));
        final UUID selectedAcademyId = UUID.randomUUID();
        final UUID resolvedAcademyId = UUID.randomUUID();

        when(securityContextFacade.getRequiredAuthenticatedUser()).thenReturn(user);
        when(tenantSelection.resolveActiveAcademyId(any())).thenReturn(Optional.of(selectedAcademyId));
        when(tenantAccessPort.resolveAcademyId(user, selectedAcademyId)).thenReturn(resolvedAcademyId);

        final TenantResolutionFilter filter = new TenantResolutionFilter(
            securityContextFacade,
            tenantSelection,
            tenantAccessPort,
            tenantContextHolder
        );

        filter.doFilter(
            request("/api/identity/me"),
            new MockHttpServletResponse(),
            filterChain
        );

        verify(tenantContextHolder).set(eq(new TenantContext(resolvedAcademyId)));
        verify(filterChain).doFilter(any(), any());
        verify(tenantContextHolder).clear();
    }

    @Test
    @DisplayName("tenantResolutionFilter_shouldAlwaysClearTenantContext_inFinally")
    void tenantResolutionFilter_shouldAlwaysClearTenantContext_inFinally() throws ServletException, IOException {
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantSelection tenantSelection = mock(TenantSelection.class);
        final TenantAccessPort tenantAccessPort = mock(TenantAccessPort.class);
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final FilterChain filterChain = mock(FilterChain.class);

        final AuthenticatedUser user = new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER));
        final UUID academyId = UUID.randomUUID();

        when(securityContextFacade.getRequiredAuthenticatedUser()).thenReturn(user);
        when(tenantSelection.resolveActiveAcademyId(any())).thenReturn(Optional.of(academyId));
        when(tenantAccessPort.resolveAcademyId(any(), any())).thenReturn(academyId);
        org.mockito.Mockito.doThrow(new IOException("boom")).when(filterChain).doFilter(any(), any());

        final TenantResolutionFilter filter = new TenantResolutionFilter(
            securityContextFacade,
            tenantSelection,
            tenantAccessPort,
            tenantContextHolder
        );

        assertThrows(IOException.class, () -> filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), filterChain));

        verify(tenantContextHolder).clear();
    }

    @Test
    @DisplayName("tenantResolutionFilter_shouldSkipResolution_whenUserDoesNotRequireTenant")
    void tenantResolutionFilter_shouldSkipResolution_whenUserDoesNotRequireTenant() throws ServletException, IOException {
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantSelection tenantSelection = mock(TenantSelection.class);
        final TenantAccessPort tenantAccessPort = mock(TenantAccessPort.class);
        final TenantContextHolder tenantContextHolder = mock(TenantContextHolder.class);
        final FilterChain filterChain = mock(FilterChain.class);

        when(securityContextFacade.getRequiredAuthenticatedUser())
            .thenReturn(new AuthenticatedUser("sub-1", "admin@test.com", Set.of(Role.ADMIN)));

        final TenantResolutionFilter filter = new TenantResolutionFilter(
            securityContextFacade,
            tenantSelection,
            tenantAccessPort,
            tenantContextHolder
        );

        filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), filterChain);

        verify(tenantSelection, never()).resolveActiveAcademyId(any());
        verify(tenantAccessPort, never()).resolveAcademyId(any(), any());
        verify(filterChain).doFilter(any(), any());
        verify(tenantContextHolder).clear();
    }

    @Test
    @DisplayName("tenantResolutionFilter_shouldNotFilter_publicAndDocsPaths")
    void tenantResolutionFilter_shouldNotFilter_publicAndDocsPaths() throws ServletException, IOException {
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantResolutionFilter filter = new TenantResolutionFilter(
            securityContextFacade,
            mock(TenantSelection.class),
            mock(TenantAccessPort.class),
            mock(TenantContextHolder.class)
        );

        final FilterChain filterChain = mock(FilterChain.class);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request("/api/public/identity/academy-registration"), response, filterChain);
        filter.doFilter(request("/actuator/health"), response, filterChain);
        filter.doFilter(request("/swagger-ui/index.html"), response, filterChain);
        filter.doFilter(request("/v3/api-docs"), response, filterChain);

        verify(securityContextFacade, never()).getRequiredAuthenticatedUser();
        verify(filterChain, org.mockito.Mockito.times(4)).doFilter(any(), any());
    }


    @Test
    @DisplayName("tenantResolutionFilter_shouldFilter_nonPublicPath")
    void tenantResolutionFilter_shouldFilter_nonPublicPath() throws ServletException, IOException {
        final SecurityContextFacade securityContextFacade = mock(SecurityContextFacade.class);
        final TenantResolutionFilter filter = new TenantResolutionFilter(
            securityContextFacade,
            mock(TenantSelection.class),
            mock(TenantAccessPort.class),
            mock(TenantContextHolder.class)
        );

        when(securityContextFacade.getRequiredAuthenticatedUser())
            .thenReturn(new AuthenticatedUser("sub-1", "admin@test.com", Set.of(Role.ADMIN)));

        filter.doFilter(request("/api/identity/me"), new MockHttpServletResponse(), mock(FilterChain.class));

        verify(securityContextFacade).getRequiredAuthenticatedUser();
    }

    private MockHttpServletRequest request(final String uri) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
