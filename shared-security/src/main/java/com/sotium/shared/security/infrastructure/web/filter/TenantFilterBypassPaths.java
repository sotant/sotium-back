package com.sotium.shared.security.infrastructure.web.filter;

final class TenantFilterBypassPaths {

    private TenantFilterBypassPaths() {
    }

    /**
     * Defines endpoints that bypass tenant resolution/enforcement while keeping JWT authentication in place.
     * Onboarding is excluded because tenant membership is created during that flow and cannot be required yet.
     */
    static boolean shouldBypass(final String path) {
        return path.startsWith("/actuator")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/api/public")
            || path.equals("/api/onboarding")
            || path.startsWith("/api/onboarding/");
    }
}
