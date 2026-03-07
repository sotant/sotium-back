package com.sotium.identity.application.port.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ListAcademyUsersPublicUseCase {

    ListAcademyUsersPublicResult list(ListAcademyUsersPublicCommand command);

    record ListAcademyUsersPublicCommand(UUID academyId) {
    }

    record ListAcademyUsersPublicResult(List<UserSummary> users) {
    }

    record UserSummary(
        UUID id,
        UUID userId,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String bio,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String email
    ) {
    }
}
