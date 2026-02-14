package com.sotium.bootstrap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.sotium", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainPackages_shouldNotDependOnSpringOrJakartaPersistence =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule applicationLayer_shouldDependOnlyOnDomainAndPorts_notOnInfrastructure =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule portOutInterfaces_shouldBeInterfaces_only =
        classes()
            .that().resideInAnyPackage(
                "com.sotium.identity.application.port.out..",
                "com.sotium.shared.security.application.port.out.."
            )
            .should().beInterfaces();

    @ArchTest
    static final ArchRule adaptersOut_shouldResideInInfrastructure_andImplementPortOut =
        classes()
            .that().resideInAPackage("com.sotium.identity.infrastructure..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().implement(com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage(
                "com.sotium.identity.application.port.out..",
                "com.sotium.shared.security.application.port.out.."
            ));

    @ArchTest
    static final ArchRule controllers_shouldNotAccessSpringDataRepositoriesDirectly =
        noClasses()
            .that().resideInAPackage("..interfaces.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure.persistence..", "org.springframework.data.jpa.repository..");

    @ArchTest
    static final ArchRule sharedSecurityFilters_shouldNotDependOnIdentityInfrastructure =
        noClasses()
            .that().resideInAPackage("com.sotium.shared.security.infrastructure.web.filter..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.sotium.identity..");

    @ArchTest
    static final ArchRule exceptionTypes_shouldStayInOwnBoundedContext =
        noClasses()
            .that().resideOutsideOfPackage("com.sotium.identity..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("com.sotium.identity.application.exception.IdentityAccessDeniedException");
}
