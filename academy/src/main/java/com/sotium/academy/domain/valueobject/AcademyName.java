package com.sotium.academy.domain.valueobject;

public record AcademyName(String value) {

    private static final int MAX_LENGTH = 255;

    public AcademyName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("academyName cannot be blank");
        }

        final String normalizedName = value.trim();
        if (normalizedName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("academyName length cannot exceed 255 characters");
        }

        value = normalizedName;
    }
}
