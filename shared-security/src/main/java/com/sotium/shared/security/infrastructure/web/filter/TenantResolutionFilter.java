package com.sotium.shared.security.infrastructure.web.filter;

import com.sotium.shared.security.application.port.out.TenantAccessPort;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.TenantContext;
import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves tenant context from request metadata and membership validation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final Set<String> TENANT_SCOPED_ROLES = Set.of("ROLE_OWNER", "ROLE_TEACHER", "ROLE_STUDENT");

    private final SecurityContextFacade securityContextFacade;
    private final TenantSelection tenantSelection;
    private final TenantAccessPort tenantAccessPort;
    private final TenantContextHolder tenantContextHolder;

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final AuthenticatedUser authenticatedUser = securityContextFacade.getRequiredAuthenticatedUser();
            final boolean requiresTenant = authenticatedUser.authorities().stream().anyMatch(TENANT_SCOPED_ROLES::contains);
            if (requiresTenant) {
                final UUID selectedAcademyId = tenantSelection.resolveActiveAcademyId(request).orElse(null);
                final UUID academyId = tenantAccessPort.resolveAcademyId(authenticatedUser, selectedAcademyId);
                tenantContextHolder.set(new TenantContext(academyId));
                log.debug("Tenant context resolved for subject={} academyId={}", authenticatedUser.sub(), academyId);
            }
            filterChain.doFilter(request, response);
        } finally {
            tenantContextHolder.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api/public");
    }
}
