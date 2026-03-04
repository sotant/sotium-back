package com.sotium.academy.application.port.in;

import com.sotium.academy.domain.model.AcademyStatus;

import java.util.UUID;

public record CreateAcademyResult(
    UUID academyId,
    AcademyStatus status
) {
}
