package com.sotium.onboarding.interfaces.web;

import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase;
import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase.RegisterAcademyCommand;
import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase.RegisterAcademyResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/onboarding/academies")
@RequiredArgsConstructor
public class OnboardingAcademyController {

    private final RegisterAcademyUseCase registerAcademyUseCase;

    @PostMapping
    public ResponseEntity<RegisterAcademyResponse> register(
        @Valid @RequestBody final RegisterAcademyRequest request,
        final JwtAuthenticationToken authentication
    ) {
        final Jwt jwt = authentication.getToken();
        final String sub = requireClaim(jwt.getSubject(), "sub");
        final String ownerEmail = requireClaim(jwt.getClaimAsString("email"), "email");

        final RegisterAcademyResult result = registerAcademyUseCase.register(
            new RegisterAcademyCommand(
                request.name(),
                request.email(),
                request.phone(),
                request.timezone(),
                sub,
                ownerEmail
            )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new RegisterAcademyResponse(result.academyId(), result.status()));
    }

    private String requireClaim(final String value, final String claimName) {
        return Objects.requireNonNull(value, "JWT %s claim is required".formatted(claimName));
    }
}
