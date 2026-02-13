package com.sotium.shared.security.infrastructure.security;

import com.sotium.identity.interfaces.web.MeController;
import com.sotium.identity.interfaces.web.PublicIdentityController;
import com.sotium.shared.security.application.port.out.TenantAccessPort;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import com.sotium.shared.security.infrastructure.web.filter.TenantContextHolder;
import com.sotium.shared.security.infrastructure.web.filter.TenantEnforcementFilter;
import com.sotium.shared.security.infrastructure.web.filter.TenantResolutionFilter;
import com.sotium.shared.security.infrastructure.web.filter.TenantSelection;
import com.sotium.shared.security.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://issuer.test/realms/sotium",
        "security.oauth2.audience=sotium-api"
    },
    classes = {
        ResourceServerConfig.class,
        MeController.class,
        PublicIdentityController.class,
        GlobalExceptionHandler.class,
        ResourceServerConfigSecurityTest.SecurityTestBeans.class
    }
)
@AutoConfigureMockMvc
class ResourceServerConfigSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("resourceServerConfig_shouldRequireAuthenticationForNonPublicEndpoints")
    void resourceServerConfig_shouldRequireAuthenticationForNonPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/identity/me"))
            .andExpect(result -> {
                final int status = result.getResponse().getStatus();
                if (status != 401 && status != 403) {
                    throw new AssertionError("Expected 401 or 403 but was " + status);
                }
            });
    }

    @Test
    @DisplayName("resourceServerConfig_shouldPermitPublicEndpointsWithoutAuthentication")
    void resourceServerConfig_shouldPermitPublicEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/identity/academy-registration"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"status":"registration endpoint available"}
                """));
    }

    @TestConfiguration
    @Import(GlobalExceptionHandler.class)
    static class SecurityTestBeans {

        @Bean
        SecurityContextFacade securityContextFacade() {
            return new SecurityContextFacade() {
                @Override
                public com.sotium.shared.security.domain.model.AuthenticatedUser getRequiredAuthenticatedUser() {
                    throw new ForbiddenException("Authenticated user is required");
                }
            };
        }

        @Bean
        TenantContextHolder tenantContextHolder() {
            return new TenantContextHolder();
        }

        @Bean
        TenantSelection tenantSelection() {
            return new TenantSelection();
        }

        @Bean
        TenantAccessPort tenantAccessPort() {
            return (authenticatedUser, selectedAcademyId) -> selectedAcademyId;
        }

        @Bean
        TenantResolutionFilter tenantResolutionFilter(
            final SecurityContextFacade securityContextFacade,
            final TenantSelection tenantSelection,
            final TenantAccessPort tenantAccessPort,
            final TenantContextHolder tenantContextHolder
        ) {
            return new TenantResolutionFilter(securityContextFacade, tenantSelection, tenantAccessPort, tenantContextHolder);
        }

        @Bean
        TenantEnforcementFilter tenantEnforcementFilter(
            final TenantContextHolder tenantContextHolder,
            final SecurityContextFacade securityContextFacade
        ) {
            return new TenantEnforcementFilter(tenantContextHolder, securityContextFacade);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new UnsupportedOperationException("Decoder should not be called in these anonymous endpoint tests");
            };
        }
    }
}
