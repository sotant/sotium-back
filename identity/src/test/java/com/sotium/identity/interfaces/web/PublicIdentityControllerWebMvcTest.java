package com.sotium.identity.interfaces.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("It doesn't work")
@WebMvcTest(controllers = PublicIdentityController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicIdentityControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("publicRegistrationProbe_shouldReturn200AndStatusPayload")
    void publicRegistrationProbe_shouldReturn200AndStatusPayload() throws Exception {
        mockMvc.perform(get("/api/public/identity/academy-registration"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {"status":"registration endpoint available"}
                """));
    }
}
