//package com.sotium.academy.infrastructure.persistence;
//
//import com.sotium.academy.domain.model.Academy;
//import com.sotium.academy.domain.model.AcademySettings;
//import com.sotium.academy.domain.valueobject.AcademyEmail;
//import com.sotium.academy.domain.valueobject.AcademyName;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@Testcontainers
//@DataJpaTest(properties = {
//    "spring.jpa.hibernate.ddl-auto=create-drop",
//    "spring.jpa.show-sql=false"
//})
//@Import({JpaAcademyRepositoryAdapter.class, JpaAcademySettingsRepositoryAdapter.class})
//class AcademyPersistenceIntegrationTest {
//
//    @Container
//    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");
//
//    @DynamicPropertySource
//    static void registerDataSourceProperties(final DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
//        registry.add("spring.datasource.username", POSTGRES::getUsername);
//        registry.add("spring.datasource.password", POSTGRES::getPassword);
//    }
//
//    @Autowired
//    private JpaAcademyRepositoryAdapter academyRepositoryAdapter;
//
//    @Autowired
//    private JpaAcademySettingsRepositoryAdapter academySettingsRepositoryAdapter;
//
//    @Autowired
//    private SpringDataAcademyRepository springDataAcademyRepository;
//
//    @Autowired
//    private SpringDataAcademySettingsRepository springDataAcademySettingsRepository;
//
//    @Test
//    @DisplayName("save_shouldPersistAcademyAndInitialSettings")
//    void save_shouldPersistAcademyAndInitialSettings() {
//        final Academy academy = academyRepositoryAdapter.save(
//            Academy.create(new AcademyName("Sotium Persisted"), new AcademyEmail("persisted@test.com"))
//        );
//
//        final AcademySettings academySettings = academySettingsRepositoryAdapter.save(
//            AcademySettings.initial(academy.id(), null, null)
//        );
//
//        final AcademyJpaEntity persistedAcademy = springDataAcademyRepository.findById(academy.id().value()).orElseThrow();
//        final AcademySettingsJpaEntity persistedSettings = springDataAcademySettingsRepository
//            .findById(academy.id().value())
//            .orElseThrow();
//
//        assertEquals("Sotium Persisted", persistedAcademy.getName());
//        assertEquals("persisted@test.com", persistedAcademy.getEmail());
//        assertEquals("UTC", persistedSettings.getTimezone());
//        assertEquals("{}", persistedSettings.getOpeningHours().toString());
//        assertEquals("[]", persistedSettings.getHolidays().toString());
//        assertEquals(academy.id().value(), academySettings.academyId().value());
//        assertNotNull(academySettings.updatedAt());
//    }
//}
