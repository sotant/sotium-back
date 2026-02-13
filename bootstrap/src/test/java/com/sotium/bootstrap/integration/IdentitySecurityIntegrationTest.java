package com.sotium.bootstrap.integration;

import com.sotium.bootstrap.SotiumApplication;
import com.sotium.identity.application.port.in.ResolveTenantContextUseCase;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import com.sotium.identity.domain.model.MembershipStatus;
import com.sotium.identity.infrastructure.persistence.IdentityUserRepositoryAdapter;
import com.sotium.identity.infrastructure.persistence.MembershipRepositoryAdapter;
import com.sotium.identity.infrastructure.persistence.SpringDataIdentityUserRepository;
import com.sotium.identity.infrastructure.persistence.SpringDataMembershipRepository;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class IdentitySecurityIntegrationTest {

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

    @Autowired
    private SpringDataIdentityUserRepository springDataIdentityUserRepository;

    @Autowired
    private SpringDataMembershipRepository springDataMembershipRepository;

    @Autowired
    private IdentityUserRepositoryAdapter identityUserRepositoryAdapter;

    @Autowired
    private MembershipRepositoryAdapter membershipRepositoryAdapter;

    @Autowired
    private ResolveTenantContextUseCase resolveTenantContextUseCase;

    @BeforeEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM academy_memberships");
        jdbcTemplate.update("DELETE FROM identity_users");
    }

    @Test
    @DisplayName("e2e_meEndpoint_shouldReturnAcademyForTenantScopedUser_withValidJwtAndMembership")
    void e2e_meEndpoint_shouldReturnAcademyForTenantScopedUser_withValidJwtAndMembership() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-owner", "owner@test.com", IdentityUserStatus.ACTIVE);
        insertMembership(UUID.randomUUID(), academyId, userId, "OWNER", "ACTIVE");

        mockMvc.perform(get("/api/identity/me")
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("sub-owner"))
            .andExpect(jsonPath("$.email").value("owner@test.com"))
            .andExpect(jsonPath("$.academyId").value(academyId.toString()));
    }

    @Test
    @DisplayName("e2e_meEndpoint_shouldReturn403_whenTenantUserWithoutMemberships")
    void e2e_meEndpoint_shouldReturn403_whenTenantUserWithoutMemberships() throws Exception {
        final UUID userId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-owner", "owner@test.com", IdentityUserStatus.ACTIVE);

        mockMvc.perform(get("/api/identity/me")
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User does not have active academy memberships"));
    }

    @Test
    @DisplayName("e2e_meEndpoint_shouldReturn403_whenMultipleMembershipsWithoutSelection")
    void e2e_meEndpoint_shouldReturn403_whenMultipleMembershipsWithoutSelection() throws Exception {
        final UUID userId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-owner", "owner@test.com", IdentityUserStatus.ACTIVE);
        insertMembership(UUID.randomUUID(), UUID.randomUUID(), userId, "OWNER", "ACTIVE");
        insertMembership(UUID.randomUUID(), UUID.randomUUID(), userId, "TEACHER", "ACTIVE");

        mockMvc.perform(get("/api/identity/me")
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Multiple memberships found; explicit academy selection is required"));
    }

    @Test
    @DisplayName("e2e_meEndpoint_shouldReturn403_whenSelectedAcademyNotBelongsToUser")
    void e2e_meEndpoint_shouldReturn403_whenSelectedAcademyNotBelongsToUser() throws Exception {
        final UUID userId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-owner", "owner@test.com", IdentityUserStatus.ACTIVE);
        insertMembership(UUID.randomUUID(), UUID.randomUUID(), userId, "OWNER", "ACTIVE");

        mockMvc.perform(get("/api/identity/me")
                .header("X-Academy-Id", UUID.randomUUID())
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Selected academy does not belong to the authenticated user"));
    }

    @Test
    @DisplayName("e2e_meEndpoint_shouldReturn400_whenHeaderAcademyIdMalformed")
    void e2e_meEndpoint_shouldReturn400_whenHeaderAcademyIdMalformed() throws Exception {
        final UUID userId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-owner", "owner@test.com", IdentityUserStatus.ACTIVE);

        mockMvc.perform(get("/api/identity/me")
                .header("X-Academy-Id", "invalid")
                .with(authenticatedUser("sub-owner", "owner@test.com", Set.of(Role.OWNER))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("e2e_publicRegistrationProbe_shouldBeAccessibleWithoutToken")
    void e2e_publicRegistrationProbe_shouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/public/identity/academy-registration"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"status":"registration endpoint available"}
                """));
    }

    @Test
    @DisplayName("repository_identityUser_findByKeycloakSub_shouldReturnEntity")
    void repository_identityUser_findByKeycloakSub_shouldReturnEntity() {
        final UUID userId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-repo", "repo@test.com", IdentityUserStatus.ACTIVE);

        final var entity = springDataIdentityUserRepository.findByKeycloakSub("sub-repo");

        assertTrue(entity.isPresent());
        assertEquals(userId, entity.get().getId());
        assertEquals("repo@test.com", entity.get().getEmail());
    }

    @Test
    @DisplayName("repository_membership_findByUserIdAndStatus_shouldFilterByActiveStatus")
    void repository_membership_findByUserIdAndStatus_shouldFilterByActiveStatus() {
        final UUID userId = UUID.randomUUID();
        final UUID activeMembershipId = UUID.randomUUID();
        insertMembership(activeMembershipId, UUID.randomUUID(), userId, "OWNER", "ACTIVE");
        insertMembership(UUID.randomUUID(), UUID.randomUUID(), userId, "OWNER", "INVITED");

        final var active = springDataMembershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);

        assertEquals(1, active.size());
        assertEquals(activeMembershipId, active.getFirst().getId());
    }

    @Test
    @DisplayName("repositoryAdapters_shouldMapJpaToDomainCorrectly")
    void repositoryAdapters_shouldMapJpaToDomainCorrectly() {
        final UUID userId = UUID.randomUUID();
        final UUID membershipId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        insertIdentityUser(userId, "sub-map", "map@test.com", IdentityUserStatus.ACTIVE);
        insertMembership(membershipId, academyId, userId, "TEACHER", "ACTIVE");

        final IdentityUser user = identityUserRepositoryAdapter.findByKeycloakSub("sub-map").orElseThrow();
        final var memberships = membershipRepositoryAdapter.findActiveMembershipsByUserId(userId);

        assertEquals(userId, user.id());
        assertEquals("sub-map", user.keycloakSub());
        assertEquals("map@test.com", user.email());
        assertEquals(IdentityUserStatus.ACTIVE, user.status());
        assertEquals(1, memberships.size());
        assertEquals(membershipId, memberships.getFirst().id());
        assertEquals(academyId, memberships.getFirst().academyId());
    }

    @Test
    @DisplayName("transaction_consistency_resolveTenantContext_shouldReadConsistentMembershipSet")
    void transaction_consistency_resolveTenantContext_shouldReadConsistentMembershipSet() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        insertIdentityUser(userId, "sub-consistency", "consistency@test.com", IdentityUserStatus.ACTIVE);
        insertMembership(UUID.randomUUID(), academyId, userId, "OWNER", "ACTIVE");
        insertMembership(UUID.randomUUID(), UUID.randomUUID(), userId, "OWNER", "INVITED");

        final UUID firstResolved = resolveTenantContextUseCase.resolveAcademyId("sub-consistency", null);
        final UUID secondResolved = resolveTenantContextUseCase.resolveAcademyId("sub-consistency", null);

        assertEquals(academyId, firstResolved);
        assertEquals(firstResolved, secondResolved);
    }

    private void insertIdentityUser(final UUID id, final String sub, final String email, final IdentityUserStatus status) {
        jdbcTemplate.update(
            "INSERT INTO identity_users (id, keycloak_sub, email, status) VALUES (?, ?, ?, ?)",
            id,
            sub,
            email,
            status.name()
        );
    }

    private void insertMembership(
        final UUID id,
        final UUID academyId,
        final UUID userId,
        final String role,
        final String status
    ) {
        jdbcTemplate.update(
            "INSERT INTO academy_memberships (id, academy_id, user_id, role, status) VALUES (?, ?, ?, ?, ?)",
            id,
            academyId,
            userId,
            role,
            status
        );
    }

    private RequestPostProcessor authenticatedUser(final String sub, final String email, final Set<Role> roles) {
        final Jwt jwt = Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", sub)
            .claim("email", email)
            .build();

        final var authorities = roles.stream()
            .map(Role::asAuthority)
            .map(SimpleGrantedAuthority::new)
            .toList();

        final JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities, sub);
        authenticationToken.setDetails(new AuthenticatedUser(sub, email, roles));

        return authentication(authenticationToken);
    }
}
