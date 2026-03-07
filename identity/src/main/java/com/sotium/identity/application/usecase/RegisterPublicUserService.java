package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.RegisterPublicUserUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.application.port.out.UserProfileRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import com.sotium.identity.domain.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterPublicUserService implements RegisterPublicUserUseCase {

    private static final String DEFAULT_AVATAR_URL = "https://mock.sotium/avatar/default";
    private static final String DEFAULT_BIO = "Mock bio";

    private final IdentityUserRepository identityUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional
    public RegisterPublicUserResult register(final RegisterPublicUserCommand command) {
        Objects.requireNonNull(command, "registerPublicUserCommand must not be null");
        final UUID academyId = Objects.requireNonNull(command.academyId(), "academyId must not be null");
        final String email = requireNonBlank(command.email(), "email");

        if (identityUserRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("email is already used");
        }

        final IdentityUser createdIdentity = identityUserRepository.save(new IdentityUser(
            UUID.randomUUID(),
            UUID.randomUUID().toString(),
            email,
            IdentityUserStatus.ACTIVE
        ));

        userProfileRepository.save(new UserProfile(
            UUID.randomUUID(),
            createdIdentity.id(),
            valueOrMock(command.name(), "Mock Name"),
            valueOrMock(command.surname(), "Mock Surname"),
            valueOrMock(command.phone(), "000000000"),
            DEFAULT_AVATAR_URL,
            DEFAULT_BIO,
            null,
            null
        ));

        final AcademyMembership createdMembership = membershipRepository.save(new AcademyMembership(
            UUID.randomUUID(),
            academyId,
            createdIdentity.id(),
            MembershipRole.USER,
            MembershipStatus.ACTIVE
        ));

        return new RegisterPublicUserResult(createdIdentity.id(), createdMembership.id(), true);
    }

    private String requireNonBlank(final String value, final String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private String valueOrMock(final String value, final String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
