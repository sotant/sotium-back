package com.sotium.academy.domain.model;

import com.sotium.academy.domain.valueobject.AcademyId;

import java.time.Instant;
import java.util.Objects;

public record AcademySettings(
    AcademyId academyId,
    String phone,
    String timezone,
    String openingHours,
    String holidays,
    Instant updatedAt
) {

    private static final String DEFAULT_TIMEZONE = "UTC";
    private static final String DEFAULT_OPENING_HOURS = "{}";
    private static final String DEFAULT_HOLIDAYS = "[]";

    public AcademySettings {
        Objects.requireNonNull(academyId, "academyId cannot be null");
        timezone = normalizeTimezone(timezone);
        openingHours = normalizeOpeningHours(openingHours);
        holidays = normalizeHolidays(holidays);
        Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    public static AcademySettings initial(final AcademyId academyId, final String phone, final String timezone) {
        return new AcademySettings(
            academyId,
            phone,
            timezone,
            DEFAULT_OPENING_HOURS,
            DEFAULT_HOLIDAYS,
            Instant.now()
        );
    }

    private static String normalizeTimezone(final String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_TIMEZONE;
        }

        return timezone.trim();
    }

    private static String normalizeOpeningHours(final String openingHours) {
        if (openingHours == null || openingHours.isBlank()) {
            return DEFAULT_OPENING_HOURS;
        }

        return openingHours;
    }

    private static String normalizeHolidays(final String holidays) {
        if (holidays == null || holidays.isBlank()) {
            return DEFAULT_HOLIDAYS;
        }

        return holidays;
    }
}
