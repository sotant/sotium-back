package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyEmail;
import com.sotium.academy.domain.valueobject.AcademyName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcademyTest {

    @Test
    @DisplayName("create_shouldThrowException_whenNameIsBlank")
    void create_shouldThrowException_whenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new AcademyName("   "));
    }

    @Test
    @DisplayName("create_shouldThrowException_whenEmailIsInvalid")
    void create_shouldThrowException_whenEmailIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new AcademyEmail("invalid-email"));
    }

    @Test
    @DisplayName("create_shouldBuildConsistentAcademy_whenInputIsValid")
    void create_shouldBuildConsistentAcademy_whenInputIsValid() {
        final Academy academy = Academy.create(new AcademyName("Sotium Academy"), new AcademyEmail("academy@test.com"));

        assertNotNull(academy.id());
        assertEquals("Sotium Academy", academy.name().value());
        assertEquals("academy@test.com", academy.email().value());
        assertEquals(AcademyStatus.ACTIVE, academy.status());
        assertNotNull(academy.createdAt());
    }
}
