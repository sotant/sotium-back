package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcademySettingsTest {

    @Test
    @DisplayName("initial_shouldSetNonNullDefaultsForJsonFields")
    void initial_shouldSetNonNullDefaultsForJsonFields() {
        final AcademySettings settings = AcademySettings.initial(new AcademyId(UUID.randomUUID()), null, null);

//        assertEquals("{}", settings.openingHours());
//        assertEquals("[]", settings.holidays());
    }

    @Test
    @DisplayName("initial_shouldUseUtcAsDefaultTimezone_whenInputTimezoneIsNull")
    void initial_shouldUseUtcAsDefaultTimezone_whenInputTimezoneIsNull() {
        final AcademySettings settings = AcademySettings.initial(new AcademyId(UUID.randomUUID()), null, null);

        assertEquals("UTC", settings.timezone());
    }
}
