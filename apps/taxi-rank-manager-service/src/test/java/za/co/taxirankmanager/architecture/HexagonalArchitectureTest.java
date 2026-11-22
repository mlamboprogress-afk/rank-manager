package za.co.taxirankmanager.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces hexagonal architecture dependency rules.
 * These tests fail the build if the boundaries are breached.
 */
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("za.co.taxirankmanager");
    }

    @Test
    void domainMustNotDependOnQuarkus() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("io.quarkus..")
                .because("Domain must be framework-free");
        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnJakarta() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta..")
                .because("Domain must not use Jakarta EE annotations");
        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnHibernate() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.hibernate..")
                .because("Domain aggregates must not be ORM entities");
        rule.check(classes);
    }

    @Test
    void applicationServicesMustNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..")
                .because("Application services must only depend on port interfaces, not adapter implementations");
        rule.check(classes);
    }

    @Test
    void restAdaptersMustNotAccessRepositoriesDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapters.in.rest..")
                .should().dependOnClassesThat().resideInAPackage("..adapters.out.postgres..")
                .because("REST controllers must call use-case ports, not repositories");
        rule.check(classes);
    }

    @Test
    void domainMustNotDependOnApplicationPorts() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..queue.domain..")
                .should().dependOnClassesThat().resideInAPackage("..queue.application.port.out..")
                .because("Domain aggregates must not depend on outbound port interfaces");
        rule.check(classes);
    }
}
