package com.sotium.onboarding.application.usecase;

import com.sotium.academy.application.port.in.AcademyRegistrationPort;
import com.sotium.academy.application.port.in.CreateAcademyCommand;
import com.sotium.academy.application.port.in.CreateAcademyResult;
import com.sotium.academy.domain.model.AcademyStatus;
import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase;
import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase.RegisterAcademyCommand;
import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase.RegisterAcademyResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterAcademyServiceTest {

    @Test
    @DisplayName("register_shouldOrchestrateEnsureCreateAssign_whenRequestIsValid")
    void register_shouldOrchestrateEnsureCreateAssign_whenRequestIsValid() {
        final List<String> calls = new ArrayList<>();
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final EnsureIdentityUserExistsFromTokenUseCase ensureUser = command -> {
            calls.add("ensure");
            assertEquals("sub-1", command.keycloakSub());
            assertEquals("owner@test.com", command.email());
            return new EnsureIdentityUserExistsFromTokenUseCase.EnsureIdentityUserResult(userId, true, false);
        };

        final AcademyRegistrationPort createAcademy = command -> {
            calls.add("academy");
            assertEquals("Academy One", command.name());
            assertEquals("academy@test.com", command.email());
            assertEquals("UTC", command.timezone());
            return new CreateAcademyResult(academyId, AcademyStatus.ACTIVE);
        };

        final AssignOwnerMembershipUseCase assignOwnerMembership = command -> {
            calls.add("membership");
            assertEquals(userId, command.userId());
            assertEquals(academyId, command.academyId());
            return new AssignOwnerMembershipUseCase.AssignOwnerMembershipResult(UUID.randomUUID(), false);
        };

        final RegisterAcademyService service = new RegisterAcademyService(ensureUser, createAcademy, assignOwnerMembership);

        final RegisterAcademyResult result = service.register(
            new RegisterAcademyCommand(
                "Academy One",
                "academy@test.com",
                "+1",
                null,
                "sub-1",
                "owner@test.com"
            )
        );

        assertEquals(academyId, result.academyId());
        assertEquals("COMPLETED", result.status());
        assertEquals(List.of("ensure", "academy", "membership"), calls);
    }

    @Test
    @DisplayName("register_shouldPropagateException_whenAssignOwnerFails")
    void register_shouldPropagateException_whenAssignOwnerFails() {
        final EnsureIdentityUserExistsFromTokenUseCase ensureUser = command ->
            new EnsureIdentityUserExistsFromTokenUseCase.EnsureIdentityUserResult(UUID.randomUUID(), true, false);
        final AcademyRegistrationPort createAcademy = command -> new CreateAcademyResult(UUID.randomUUID(), AcademyStatus.ACTIVE);
        final AssignOwnerMembershipUseCase assignOwnerMembership = command -> {
            throw new IllegalStateException("membership assignment failed");
        };

        final RegisterAcademyService service = new RegisterAcademyService(ensureUser, createAcademy, assignOwnerMembership);

        assertThrows(
            IllegalStateException.class,
            () -> service.register(
                new RegisterAcademyCommand(
                    "Academy One",
                    "academy@test.com",
                    null,
                    "UTC",
                    "sub-1",
                    "owner@test.com"
                )
            )
        );
    }
}
