package com.sotium.identity.interfaces.web;

import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase;
import com.sotium.identity.application.port.in.ListAcademyUsersPublicUseCase;
import com.sotium.identity.application.port.in.RegisterPublicUserUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/identity")
@Slf4j
@RequiredArgsConstructor
public class PublicIdentityController {

    private final DeleteIdentityBySubUseCase deleteIdentityBySubUseCase;
    private final RegisterPublicUserUseCase registerPublicUserUseCase;
    private final ListAcademyUsersPublicUseCase listAcademyUsersPublicUseCase;

    @GetMapping("/academy-registration")
    public ResponseEntity<Map<String, String>> registrationProbe() {
        log.debug("Public academy registration probe requested");
        return ResponseEntity.ok(Map.of("status", "registration endpoint available"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<PublicUserResponse>> listAcademyUsers(@RequestParam final UUID academyId) {
        final ListAcademyUsersPublicUseCase.ListAcademyUsersPublicResult result = listAcademyUsersPublicUseCase.list(
            new ListAcademyUsersPublicUseCase.ListAcademyUsersPublicCommand(academyId)
        );

        final List<PublicUserResponse> response = result.users()
            .stream()
            .map(user -> new PublicUserResponse(
                user.id(),
                user.userId(),
                user.firstName(),
                user.lastName(),
                user.phone(),
                user.avatarUrl(),
                user.bio(),
                user.createdAt(),
                user.updatedAt(),
                user.email()
            ))
            .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-user")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody final RegisterPublicUserRequest request) {
        final RegisterPublicUserUseCase.RegisterPublicUserResult result = registerPublicUserUseCase.register(
            new RegisterPublicUserUseCase.RegisterPublicUserCommand(
                request.academyId(),
                request.email(),
                request.name(),
                request.surname(),
                request.phone()
            )
        );
        return ResponseEntity.ok(Map.of(
            "created", result.created(),
            "userId", result.userId(),
            "membershipId", result.membershipId()
        ));
    }

    @PostMapping("/purge-by-sub")
    public ResponseEntity<Map<String, Object>> purgeBySub(@RequestBody final DeleteIdentityBySubRequest request) {
        final var result = deleteIdentityBySubUseCase.delete(new DeleteIdentityBySubUseCase.DeleteIdentityBySubCommand(request.sub()));
        return ResponseEntity.ok(Map.of("deleted", result.deleted()));
    }

    public record DeleteIdentityBySubRequest(String sub) {
    }

    public record RegisterPublicUserRequest(
        @NotNull UUID academyId,
        @NotBlank @Email String email,
        String name,
        String surname,
        String phone
    ) {
    }

    public record PublicUserResponse(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String bio,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String email
    ) {
    }
}
