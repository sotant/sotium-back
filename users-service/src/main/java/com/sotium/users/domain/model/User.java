package com.sotium.users.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record User(
    Long id,
    String email,
    String passwordHash,
    Boolean active,
    LocalDate fechaCreacion,
    LocalDateTime updatedAt
) {}
