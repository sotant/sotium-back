package com.sotium.shared.security.infrastructure.web.filter;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantSelectionTest {

    private final TenantSelection tenantSelection = new TenantSelection();

    @Test
    @DisplayName("tenantSelection_shouldPreferHeaderOverCookie")
    void tenantSelection_shouldPreferHeaderOverCookie() {
        final UUID headerAcademyId = UUID.randomUUID();
        final UUID cookieAcademyId = UUID.randomUUID();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Academy-Id", headerAcademyId.toString());
        request.setCookies(new Cookie("academy_id", cookieAcademyId.toString()));

        final Optional<UUID> resolved = tenantSelection.resolveActiveAcademyId(request);

        assertEquals(Optional.of(headerAcademyId), resolved);
    }

    @Test
    @DisplayName("tenantSelection_shouldUseCookie_whenHeaderMissing")
    void tenantSelection_shouldUseCookie_whenHeaderMissing() {
        final UUID cookieAcademyId = UUID.randomUUID();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("academy_id", cookieAcademyId.toString()));

        final Optional<UUID> resolved = tenantSelection.resolveActiveAcademyId(request);

        assertEquals(Optional.of(cookieAcademyId), resolved);
    }

    @Test
    @DisplayName("tenantSelection_shouldReturnEmpty_whenNoHeaderNoCookies")
    void tenantSelection_shouldReturnEmpty_whenNoHeaderNoCookies() {
        final Optional<UUID> resolved = tenantSelection.resolveActiveAcademyId(new MockHttpServletRequest());

        assertTrue(resolved.isEmpty());
    }

    @Test
    @DisplayName("tenantSelection_shouldThrowIllegalArgument_whenUuidMalformed")
    void tenantSelection_shouldThrowIllegalArgument_whenUuidMalformed() {
        final MockHttpServletRequest malformedHeaderRequest = new MockHttpServletRequest();
        malformedHeaderRequest.addHeader("X-Academy-Id", "not-a-uuid");

        final IllegalArgumentException malformedHeader = assertThrows(
            IllegalArgumentException.class,
            () -> tenantSelection.resolveActiveAcademyId(malformedHeaderRequest)
        );

        assertEquals("Invalid UUID string: not-a-uuid", malformedHeader.getMessage());

        final MockHttpServletRequest malformedCookieRequest = new MockHttpServletRequest();
        malformedCookieRequest.setCookies(new Cookie("academy_id", "still-not-a-uuid"));

        final IllegalArgumentException malformedCookie = assertThrows(
            IllegalArgumentException.class,
            () -> tenantSelection.resolveActiveAcademyId(malformedCookieRequest)
        );

        assertEquals("Invalid UUID string: still-not-a-uuid", malformedCookie.getMessage());
    }
}
