package com.sotium.shared.security.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServerConfigJwtDecoderTest {

    @Test
    @DisplayName("resourceServerConfig_shouldRejectTokenWithMissingAudience")
    void resourceServerConfig_shouldRejectTokenWithMissingAudience() throws Exception {
        final ResourceServerConfig config = new ResourceServerConfig();
        final NimbusJwtDecoder decoder = (NimbusJwtDecoder) config.jwtDecoder(
            "http://issuer.test/realms/sotium",
            "required-audience"
        );

        final OAuth2TokenValidator<Jwt> validator = validator(decoder);
        final Jwt tokenWithoutAudience = new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("alg", "none"),
            Map.of("sub", "sub-1", "iss", "http://issuer.test/realms/sotium", "aud", List.of("other-audience"))
        );

        final OAuth2TokenValidatorResult validation = validator.validate(tokenWithoutAudience);

        assertTrue(validation.hasErrors());
        assertEquals("invalid_token", validation.getErrors().iterator().next().getErrorCode());
    }

    @SuppressWarnings("unchecked")
    private OAuth2TokenValidator<Jwt> validator(final NimbusJwtDecoder decoder) throws Exception {
        final Field field = NimbusJwtDecoder.class.getDeclaredField("jwtValidator");
        field.setAccessible(true);
        return (OAuth2TokenValidator<Jwt>) field.get(decoder);
    }
}
