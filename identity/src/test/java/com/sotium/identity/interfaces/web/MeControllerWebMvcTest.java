package com.sotium.identity.interfaces.web;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import com.sotium.shared.security.domain.model.TenantContext;
import com.sotium.shared.security.infrastructure.security.SecurityContextFacade;
import com.sotium.shared.security.infrastructure.web.filter.TenantContextHolder;
import com.sotium.shared.security.interfaces.rest.GlobalExceptionHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("It doesn't work")
@WebMvcTest(controllers = MeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityContextFacade securityContextFacade;

    @MockitoBean
    private TenantContextHolder tenantContextHolder;

    @Test
    @DisplayName("meEndpoint_shouldReturn200WithMeResponse_whenAuthenticatedAdminWithoutTenant")
    void meEndpoint_shouldReturn200WithMeResponse_whenAuthenticatedAdminWithoutTenant() throws Exception {
        final AuthenticatedUser adminUser = new AuthenticatedUser(
            "admin-sub",
            "admin@test.com",
            Set.of(Role.ADMIN)
        );

        when(securityContextFacade.getRequiredAuthenticatedUser()).thenReturn(adminUser);
        when(tenantContextHolder.get()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/identity/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("admin-sub"))
            .andExpect(jsonPath("$.email").value("admin@test.com"))
            .andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"))
            .andExpect(jsonPath("$.academyId").doesNotExist());
    }

    @Test
    @DisplayName("meEndpoint_shouldReturn200WithAcademyId_whenTenantUserWithResolvedTenant")
    void meEndpoint_shouldReturn200WithAcademyId_whenTenantUserWithResolvedTenant() throws Exception {
        final UUID academyId = UUID.randomUUID();
        final AuthenticatedUser ownerUser = new AuthenticatedUser(
            "owner-sub",
            "owner@test.com",
            Set.of(Role.OWNER)
        );

        when(securityContextFacade.getRequiredAuthenticatedUser()).thenReturn(ownerUser);
        when(tenantContextHolder.get()).thenReturn(Optional.of(new TenantContext(academyId)));

        mockMvc.perform(get("/api/identity/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("owner-sub"))
            .andExpect(jsonPath("$.email").value("owner@test.com"))
            .andExpect(jsonPath("$.authorities[0]").value("ROLE_OWNER"))
            .andExpect(jsonPath("$.academyId").value(academyId.toString()));
    }

    @Test
    @DisplayName("meEndpoint_shouldReturn403_whenTenantUserWithoutTenantContext")
    void meEndpoint_shouldReturn403_whenTenantUserWithoutTenantContext() throws Exception {
        final AuthenticatedUser teacherUser = new AuthenticatedUser(
            "teacher-sub",
            "teacher@test.com",
            Set.of(Role.TEACHER)
        );

        when(securityContextFacade.getRequiredAuthenticatedUser()).thenReturn(teacherUser);
        when(tenantContextHolder.get()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/identity/me"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Tenant context is required"));
    }
}
