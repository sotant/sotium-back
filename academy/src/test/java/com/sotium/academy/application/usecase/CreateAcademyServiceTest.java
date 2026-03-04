package com.sotium.academy.application.usecase;

import com.sotium.academy.application.port.in.CreateAcademyCommand;
import com.sotium.academy.application.port.in.CreateAcademyResult;
import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.academy.domain.model.Academy;
import com.sotium.academy.domain.model.AcademySettings;
import com.sotium.academy.domain.model.AcademyStatus;
import com.sotium.academy.domain.valueobject.AcademyEmail;
import com.sotium.academy.domain.valueobject.AcademyId;
import com.sotium.academy.domain.valueobject.AcademyName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAcademyServiceTest {

    @Test
    @DisplayName("createAcademy_shouldPersistAcademyAndSettings_andReturnResult")
    void createAcademy_shouldPersistAcademyAndSettings_andReturnResult() {
        final InMemoryAcademyRepository academyRepository = new InMemoryAcademyRepository();
        final InMemoryAcademySettingsRepository settingsRepository = new InMemoryAcademySettingsRepository();
        final CreateAcademyService service = new CreateAcademyService(academyRepository, settingsRepository);

        final CreateAcademyResult result = service.createAcademy(
            new CreateAcademyCommand("Sotium Academy", "academy@test.com", null, null)
        );

        assertTrue(academyRepository.saveCalled);
        assertTrue(settingsRepository.saveCalled);
        assertNotNull(result.academyId());
        assertEquals(AcademyStatus.ACTIVE, result.status());
        assertEquals(result.academyId(), settingsRepository.lastSavedSettings.academyId().value());
        assertEquals("UTC", settingsRepository.lastSavedSettings.timezone());
    }

    private static final class InMemoryAcademyRepository implements AcademyRepositoryPort {

        private boolean saveCalled;

        @Override
        public Academy save(final Academy academy) {
            saveCalled = true;
            return new Academy(
                new AcademyId(UUID.randomUUID()),
                new AcademyName(academy.name().value()),
                new AcademyEmail(academy.email().value()),
                academy.status(),
                Instant.now()
            );
        }
    }

    private static final class InMemoryAcademySettingsRepository implements AcademySettingsRepositoryPort {

        private boolean saveCalled;
        private AcademySettings lastSavedSettings;

        @Override
        public AcademySettings save(final AcademySettings academySettings) {
            saveCalled = true;
            lastSavedSettings = academySettings;
            return academySettings;
        }
    }
}
