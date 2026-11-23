package com.gonzalobalay.roombooking;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

class ArchitectureTests {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.gonzalobalay.roombooking");

    @Test
    void domainDependsOnlyOnTheJdk() {
        classes().that().resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..")
                .check(CLASSES);
    }

    @Test
    void applicationDoesNotDependOnInfrastructure() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(CLASSES);
    }

    @Test
    void hexagonalLayersAreRespected() {
        onionArchitecture()
                .withOptionalLayers(true)
                .domainModels("..domain..")
                .applicationServices("..application..")
                .adapter("web", "..infrastructure.in..")
                .adapter("persistence", "..infrastructure.out..")
                .ignoreDependency(resideInAnyPackage("..infrastructure.config..", "..infrastructure.security.."), alwaysTrue())
                .check(CLASSES);
    }
}