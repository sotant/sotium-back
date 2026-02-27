package com.sotium.academy.interfaces.web;

import java.util.UUID;

public record AcademyResponse(
    UUID id,
    String name,
    boolean active
) {
}
