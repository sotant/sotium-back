package com.sotium.shared.security.infrastructure.security;

import com.sotium.shared.security.infrastructure.web.filter.TenantContextHolder;
import com.sotium.shared.security.infrastructure.web.filter.TenantEnforcementFilter;
import com.sotium.shared.security.infrastructure.web.filter.TenantResolutionFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures Keycloak JWT validation and request tenant filters for the resource server.
 */
@Configuration
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    SecurityFilterChain resourceServerFilterChain(
        final HttpSecurity http,
        final TenantResolutionFilter tenantResolutionFilter,
        final TenantEnforcementFilter tenantEnforcementFilter
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(new KeycloakRealmRoleJwtAuthenticationConverter()))
            )
            .addFilterAfter(tenantResolutionFilter, BearerTokenAuthenticationFilter.class)
            .addFilterAfter(tenantEnforcementFilter, TenantResolutionFilter.class)
            .exceptionHandling(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") final String issuerUri,
        @Value("${security.oauth2.audience}") final String audience
    ) {
        final String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        final OAuth2TokenValidator<Jwt> defaultWithIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        final OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(audience)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultWithIssuer, audienceValidator));
        return jwtDecoder;
    }

    @Bean
    TenantContextHolder tenantContextHolder() {
        return new TenantContextHolder();
    }
}
