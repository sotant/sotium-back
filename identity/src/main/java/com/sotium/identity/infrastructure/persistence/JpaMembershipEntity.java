package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
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
@Table(name = "academy_memberships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaMembershipEntity {

    @Id
    private UUID id;

    @Column(name = "academy_id", nullable = false)
    private UUID academyId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;
}
