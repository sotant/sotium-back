package com.sotium.bootstrap.integration;

import com.sotium.bootstrap.SotiumApplication;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = SotiumApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://issuer.test/realms/sotium",
    "security.oauth2.audience=sotium-api"
})
class OnboardingRegistrationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerDatasource(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM academy_memberships");
        jdbcTemplate.update("DELETE FROM academy_settings");
        jdbcTemplate.update("DELETE FROM academies");
        jdbcTemplate.update("DELETE FROM identity_users");
    }


    @Test
    @DisplayName("security_registerAcademy_shouldReturnUnauthorized_whenNoToken")
    void security_registerAcademy_shouldReturnUnauthorized_whenNoToken() throws Exception {
        mockMvc.perform(post("/api/onboarding/academies")
                .contentType("application/json")
                .content("""
                    {
                      "name":"Academy One",
                      "email":"academy-one@test.com"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("security_identityMe_shouldDenyTenantScopedEndpoint_whenTokenWithoutTenant")
    void security_identityMe_shouldDenyTenantScopedEndpoint_whenTokenWithoutTenant() throws Exception {
        mockMvc.perform(get("/api/identity/me")
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("e2e_registerAcademy_shouldCreateIdentityAcademyAndOwnerMembership")
    void e2e_registerAcademy_shouldCreateIdentityAcademyAndOwnerMembership() throws Exception {
        mockMvc.perform(post("/api/onboarding/academies")
                .with(authenticatedUser("sub-admin", "admin@test.com", Set.of(Role.ADMIN)))
                .contentType("application/json")
                .content("""
                    {
                      "name":"Academy One",
                      "email":"academy-one@test.com",
                      "phone":"12345",
                      "timezone":"UTC"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM identity_users", Integer.class));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM academies", Integer.class));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM academy_settings", Integer.class));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM academy_memberships", Integer.class));
    }

    private RequestPostProcessor authenticatedUser(final String sub, final String email, final Set<Role> roles) {
        final Jwt jwt = Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", sub)
            .claim("email", email)
            .build();

        final var authorities = roles.stream().map(Role::asAuthority).map(SimpleGrantedAuthority::new).toList();
        final JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities, sub);
        authenticationToken.setDetails(new AuthenticatedUser(sub, email, roles));
        return authentication(authenticationToken);
    }
}
