package com.accierta.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.accierta",
        importOptions = {ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_outer_layers_or_frameworks = noClasses()
            .that().resideInAnyPackage("..domain.model..", "..domain.event..", "..domain.service..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..api..",
                    "..application..",
                    "..infrastructure..",
                    "org.springframework..",
                    "jakarta..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_api_or_infrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..api..",
                    "..infrastructure..");
}
