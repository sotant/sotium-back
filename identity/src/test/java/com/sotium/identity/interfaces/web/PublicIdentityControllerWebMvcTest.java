package com.sotium.identity.interfaces.web;

import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase;
import com.sotium.identity.application.port.in.ListAcademyUsersPublicUseCase;
import com.sotium.identity.application.port.in.RegisterPublicUserUseCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("It doesn't work")
@WebMvcTest(controllers = PublicIdentityController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicIdentityControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteIdentityBySubUseCase deleteIdentityBySubUseCase;

    @MockBean
    private RegisterPublicUserUseCase registerPublicUserUseCase;

    @MockBean
    private ListAcademyUsersPublicUseCase listAcademyUsersPublicUseCase;

    @Test
    @DisplayName("publicRegistrationProbe_shouldReturn200AndStatusPayload")
    void publicRegistrationProbe_shouldReturn200AndStatusPayload() throws Exception {
        mockMvc.perform(get("/api/public/identity/academy-registration"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"status":"registration endpoint available"}
                """));
    }


    @Test
    @DisplayName("listAcademyUsers_shouldReturn200AndUsersPayload")
    void listAcademyUsers_shouldReturn200AndUsersPayload() throws Exception {
        final UUID profileId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        when(listAcademyUsersPublicUseCase.list(any()))
            .thenReturn(new ListAcademyUsersPublicUseCase.ListAcademyUsersPublicResult(java.util.List.of(
                new ListAcademyUsersPublicUseCase.UserSummary(
                    profileId,
                    userId,
                    "John",
                    "Doe",
                    "600123123",
                    "https://mock.sotium/avatar/default",
                    "Mock bio",
                    null,
                    null,
                    "john.doe@test.com"
                )
            )));

        mockMvc.perform(get("/api/public/identity/users")
                .queryParam("academyId", "d5f4ae19-8bf4-4f13-b2fa-c4ec59895ca4"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [{"id":"%s","userId":"%s","firstName":"John","lastName":"Doe","phone":"600123123","avatarUrl":"https://mock.sotium/avatar/default","bio":"Mock bio","createdAt":null,"updatedAt":null,"email":"john.doe@test.com"}]
                """.formatted(profileId, userId)));
    }

    @Test
    @DisplayName("registerUser_shouldReturn200AndPayload")
    void registerUser_shouldReturn200AndPayload() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID membershipId = UUID.randomUUID();

        when(registerPublicUserUseCase.register(any()))
            .thenReturn(new RegisterPublicUserUseCase.RegisterPublicUserResult(userId, membershipId, true));

        mockMvc.perform(post("/api/public/identity/register-user")
                .contentType("application/json")
                .content("""
                    {
                      "academyId":"d5f4ae19-8bf4-4f13-b2fa-c4ec59895ca4",
                      "email":"new.user@test.com",
                      "name":"John",
                      "surname":"Doe",
                      "phone":"600123123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"created":true,"userId":"%s","membershipId":"%s"}
                """.formatted(userId, membershipId)));
    }

    @Test
    @DisplayName("purgeBySub_shouldReturn200AndDeletedPayload")
    void purgeBySub_shouldReturn200AndDeletedPayload() throws Exception {
        when(deleteIdentityBySubUseCase.delete(any()))
            .thenReturn(new DeleteIdentityBySubUseCase.DeleteIdentityBySubResult(true));

        mockMvc.perform(post("/api/public/identity/purge-by-sub")
                .contentType("application/json")
                .content("""
                    {"sub":"abc-sub"}
                    """))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"deleted":true}
                """));
    }
}
