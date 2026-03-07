package com.sotium.onboarding.application.usecase;

import com.sotium.academy.application.port.in.AcademyRegistrationPort;
import com.sotium.academy.application.port.in.CreateAcademyCommand;
import com.sotium.academy.application.port.in.CreateAcademyResult;
import com.sotium.identity.application.exception.IdentityUserEmailConflictException;
import com.sotium.identity.application.port.in.AssignOwnerMembershipUseCase;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase;
import com.sotium.onboarding.application.exception.RegisterAcademyConflictException;
import com.sotium.onboarding.application.port.in.RegisterAcademyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RegisterAcademyService implements RegisterAcademyUseCase {

    private static final String DEFAULT_TIMEZONE = "UTC";

    private final EnsureIdentityUserExistsFromTokenUseCase ensureIdentityUserExistsFromTokenUseCase;
    private final AcademyRegistrationPort academyRegistrationPort;
    private final AssignOwnerMembershipUseCase assignOwnerMembershipUseCase;

    @Override
    @Transactional
    public RegisterAcademyResult register(final RegisterAcademyCommand command) {
        final RegisterAcademyCommand safeCommand = validate(command);

        try {
            final var ensuredUser = ensureIdentityUserExistsFromTokenUseCase.ensure(
                new EnsureIdentityUserExistsFromTokenUseCase.EnsureIdentityUserCommand(
                    safeCommand.ownerSub(),
                    safeCommand.ownerEmail()
                )
            );

            final CreateAcademyResult academyResult = academyRegistrationPort.createAcademy(
                new CreateAcademyCommand(
                    safeCommand.academyName(),
                    safeCommand.ownerEmail(),
                    safeCommand.phone(),
                    safeCommand.timezone()
                )
            );

            assignOwnerMembershipUseCase.assign(
                new AssignOwnerMembershipUseCase.AssignOwnerMembershipCommand(
                    ensuredUser.userId(),
                    academyResult.academyId()
                )
            );

            return new RegisterAcademyResult(academyResult.academyId(), "COMPLETED");
        } catch (IdentityUserEmailConflictException | DataIntegrityViolationException exception) {
            throw new RegisterAcademyConflictException(
                "Could not register academy due to a conflicting state",
                exception
            );
        }
    }

    private RegisterAcademyCommand validate(final RegisterAcademyCommand command) {
        Objects.requireNonNull(command, "registerAcademyCommand must not be null");
        requireNonBlank(command.academyName(), "academyName");
        requireNonBlank(command.ownerSub(), "ownerSub");
        requireNonBlank(command.ownerEmail(), "ownerEmail");

        final String timezone = command.timezone() == null || command.timezone().isBlank()
            ? DEFAULT_TIMEZONE
            : command.timezone();

        return new RegisterAcademyCommand(
            command.academyName(),
            command.phone(),
            timezone,
            command.ownerSub(),
            command.ownerEmail()
        );
    }

    private void requireNonBlank(final String value, final String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
