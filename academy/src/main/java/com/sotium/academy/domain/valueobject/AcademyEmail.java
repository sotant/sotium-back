package com.sotium.academy.domain.valueobject;

import java.util.regex.Pattern;

public record AcademyEmail(String value) {

    private static final int MAX_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public AcademyEmail {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("academyEmail cannot be blank");
        }

        final String normalizedEmail = value.trim().toLowerCase();
        if (normalizedEmail.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("academyEmail length cannot exceed 255 characters");
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("academyEmail format is invalid");
        }

        value = normalizedEmail;
    }
}
