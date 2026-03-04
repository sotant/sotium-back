package com.sotium.academy.application.usecase;

import com.sotium.academy.application.port.in.AcademyRegistrationPort;
import com.sotium.academy.application.port.in.CreateAcademyCommand;
import com.sotium.academy.application.port.in.CreateAcademyResult;
import com.sotium.academy.application.port.in.CreateAcademyUseCase;
import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.valueobject.AcademyEmail;
import com.sotium.academy.domain.valueobject.AcademyName;

import java.util.Objects;

public class CreateAcademyService implements AcademyRegistrationPort, CreateAcademyUseCase {

    private final AcademyRepositoryPort academyRepositoryPort;
    private final AcademySettingsRepositoryPort academySettingsRepositoryPort;

    public CreateAcademyService(
        final AcademyRepositoryPort academyRepositoryPort,
        final AcademySettingsRepositoryPort academySettingsRepositoryPort
    ) {
        this.academyRepositoryPort = Objects.requireNonNull(academyRepositoryPort, "academyRepositoryPort cannot be null");
        this.academySettingsRepositoryPort = Objects.requireNonNull(
            academySettingsRepositoryPort,
            "academySettingsRepositoryPort cannot be null"
        );
    }

    @Override
    public CreateAcademyResult createAcademy(final CreateAcademyCommand command) {
        Objects.requireNonNull(command, "createAcademyCommand cannot be null");

        final Academy academy = Academy.create(
            new AcademyName(command.name()),
            new AcademyEmail(command.email())
        );

        final Academy persistedAcademy = academyRepositoryPort.save(academy);
        final AcademySettings academySettings = AcademySettings.initial(
            persistedAcademy.id(),
            command.phone(),
            command.timezone()
        );
        academySettingsRepositoryPort.save(academySettings);

        return new CreateAcademyResult(persistedAcademy.id().value(), persistedAcademy.status());
    }
}
