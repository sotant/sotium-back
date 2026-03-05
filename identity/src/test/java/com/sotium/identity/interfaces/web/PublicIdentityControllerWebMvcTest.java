package com.sotium.identity.interfaces.web;

import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

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
