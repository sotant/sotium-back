package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.IdentityUserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "identity_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaIdentityUserEntity {

    @Id
    private UUID id;

    @Column(name = "keycloak_sub", nullable = false, unique = true)
    private String keycloakSub;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentityUserStatus status;
}
