package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.ListAcademyUsersPublicUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.application.port.out.UserProfileRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListAcademyUsersPublicService implements ListAcademyUsersPublicUseCase {

    private final MembershipRepository membershipRepository;
    private final UserProfileRepository userProfileRepository;
    private final IdentityUserRepository identityUserRepository;

    @Override
    @Transactional(readOnly = true)
    public ListAcademyUsersPublicResult list(final ListAcademyUsersPublicCommand command) {
        Objects.requireNonNull(command, "listAcademyUsersPublicCommand must not be null");
        final UUID academyId = Objects.requireNonNull(command.academyId(), "academyId must not be null");

        final List<UserSummary> users = membershipRepository.findByAcademyId(academyId)
            .stream()
            .map(AcademyMembership::userId)
            .distinct()
            .map(this::toUserSummary)
            .toList();

        return new ListAcademyUsersPublicResult(users);
    }

    private UserSummary toUserSummary(final UUID userId) {
        final UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("User profile not found for userId: " + userId));
        final IdentityUser identityUser = identityUserRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("Identity user not found for userId: " + userId));

        return new UserSummary(
            profile.id(),
            profile.userId(),
            profile.firstName(),
            profile.lastName(),
            profile.phone(),
            profile.avatarUrl(),
            profile.bio(),
            profile.createdAt(),
            profile.updatedAt(),
            identityUser.email()
        );
    }
}
