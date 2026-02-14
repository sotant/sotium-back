package com.sotium.shared.security.infrastructure.web.filter;

import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantEnforcementFilter extends OncePerRequestFilter {

    private final TenantContextHolder tenantContextHolder;
    private final SecurityContextFacade securityContextFacade;

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain
    ) throws ServletException, IOException {
        final var authenticatedUser = securityContextFacade.getRequiredAuthenticatedUser();
        if (!authenticatedUser.isAdmin() && tenantContextHolder.get().isEmpty()) {
            log.warn("Tenant context missing for subject={} path={}", authenticatedUser.sub(), request.getRequestURI());
            throw new ForbiddenException("Tenant context is required for this request");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/api/public");
    }
}
