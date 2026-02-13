package com.sotium.shared.security.interfaces.rest;

import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerWebMvcTest.ThrowingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("globalExceptionHandler_shouldMapForbiddenExceptionTo403ApiError")
    void globalExceptionHandler_shouldMapForbiddenExceptionTo403ApiError() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value("forbidden message"))
            .andExpect(jsonPath("$.path").value("/test/forbidden"));
    }

    @Test
    @DisplayName("globalExceptionHandler_shouldMapAuthenticationExceptionTo401ApiError")
    void globalExceptionHandler_shouldMapAuthenticationExceptionTo401ApiError() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.message").value("unauthorized message"))
            .andExpect(jsonPath("$.path").value("/test/unauthorized"));
    }

    @Test
    @DisplayName("globalExceptionHandler_shouldMapIllegalArgumentTo400ApiError")
    void globalExceptionHandler_shouldMapIllegalArgumentTo400ApiError() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("bad request message"))
            .andExpect(jsonPath("$.path").value("/test/bad-request"));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/test/forbidden")
        String forbidden() {
            throw new ForbiddenException("forbidden message");
        }

        @GetMapping("/test/unauthorized")
        String unauthorized() {
            throw new SimpleAuthenticationException("unauthorized message");
        }

        @GetMapping("/test/bad-request")
        String badRequest() {
            throw new IllegalArgumentException("bad request message");
        }
    }

    static final class SimpleAuthenticationException extends AuthenticationException {
        private SimpleAuthenticationException(final String message) {
            super(message);
        }
    }
}
