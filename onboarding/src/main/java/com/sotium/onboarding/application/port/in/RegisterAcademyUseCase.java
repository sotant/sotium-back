package com.sotium.onboarding.application.port.in;

import java.util.UUID;

public interface RegisterAcademyUseCase {

    RegisterAcademyResult register(RegisterAcademyCommand command);

    record RegisterAcademyCommand(
        String academyName,
        String academyEmail,
        String phone,
        String timezone,
        String ownerSub,
        String ownerEmail
    ) {
    }

    record RegisterAcademyResult(UUID academyId, String status) {
    }
}
