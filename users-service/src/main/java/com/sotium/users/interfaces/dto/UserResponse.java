package com.sotium.users.interfaces.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    Boolean active,
    LocalDate fechaCreacion,
    LocalDateTime updatedAt
) {}
