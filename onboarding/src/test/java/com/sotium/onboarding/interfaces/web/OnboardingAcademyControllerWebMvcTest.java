//package com.sotium.onboarding.interfaces.web;
//
//import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase;
//import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase.RegisterAcademyResult;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = OnboardingAcademyController.class)
//@AutoConfigureMockMvc(addFilters = false)
//@Import(OnboardingExceptionHandler.class)
//class OnboardingAcademyControllerWebMvcTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private RegisterAcademyUseCase registerAcademyUseCase;
//
//    @Test
//    @DisplayName("register_shouldReturn201_whenRequestIsValid")
//    void register_shouldReturn201_whenRequestIsValid() throws Exception {
//        final UUID academyId = UUID.randomUUID();
//        when(registerAcademyUseCase.register(any())).thenReturn(new RegisterAcademyResult(academyId, "COMPLETED"));
//
//        mockMvc.perform(post("/api/onboarding/academies")
//                .with(authentication(jwtAuth("sub-1", "owner@test.com")))
//                .contentType("application/json")
//                .content("""
//                    {
//                      "name":"Academy One",
//                      "email":"academy@test.com",
//                      "phone":"123",
//                      "timezone":"UTC"
//                    }
//                    """))
//            .andExpect(status().isCreated())
//            .andExpect(jsonPath("$.academyId").value(academyId.toString()))
//            .andExpect(jsonPath("$.status").value("COMPLETED"));
//    }
//
//    @Test
//    @DisplayName("register_shouldReturn400_whenRequestIsInvalid")
//    void register_shouldReturn400_whenRequestIsInvalid() throws Exception {
//        mockMvc.perform(post("/api/onboarding/academies")
//                .with(authentication(jwtAuth("sub-1", "owner@test.com")))
//                .contentType("application/json")
//                .content("""
//                    {
//                      "name":"",
//                      "email":"invalid"
//                    }
//                    """))
//            .andExpect(status().isBadRequest())
//            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
//    }
//
//    private JwtAuthenticationToken jwtAuth(final String sub, final String email) {
//        final Jwt jwt = Jwt.withTokenValue("token")
//            .header("alg", "none")
//            .claim("sub", sub)
//            .claim("email", email)
//            .build();
//
//        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), sub);
//    }
//}
