package com.sotium.academy.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAcademyRepository extends JpaRepository<AcademyJpaEntity, UUID> {
}
