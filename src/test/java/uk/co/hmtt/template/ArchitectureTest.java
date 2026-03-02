package uk.co.hmtt.template;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture rules enforced at test time via ArchUnit. All rules reference classes in the {@code
 * uk.co.hmtt.template} package tree.
 */
@AnalyzeClasses(
    packages = "uk.co.hmtt.template",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  @ArchTest
  static final ArchRule controllers_should_not_access_repositories_directly =
      noClasses()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .dependOnClassesThat()
          .areAssignableTo(JpaRepository.class)
          .as("@RestController classes must not reference JpaRepository types directly");

  @ArchTest
  static final ArchRule repositories_should_not_depend_on_services =
      noClasses()
          .that()
          .areAssignableTo(JpaRepository.class)
          .should()
          .dependOnClassesThat()
          .haveSimpleNameEndingWith("Service")
          .as("Repository interfaces must not depend on Service classes");

  @ArchTest
  static final ArchRule item_package_should_not_depend_on_other_feature_packages =
      noClasses()
          .that()
          .resideInAPackage("uk.co.hmtt.template.item..")
          .should()
          .dependOnClassesThat(
              JavaClass.Predicates.resideInAPackage("uk.co.hmtt.template..")
                  .and(not(resideInAPackage("uk.co.hmtt.template.item..")))
                  .and(not(resideInAPackage("uk.co.hmtt.template.common..")))
                  .and(not(resideInAPackage("uk.co.hmtt.template"))))
          .as("Classes in the 'item' package must not depend on other feature packages");

  @ArchTest
  static final ArchRule common_package_should_not_depend_on_feature_packages =
      noClasses()
          .that()
          .resideInAPackage("uk.co.hmtt.template.common..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("uk.co.hmtt.template.item..")
          .as("Classes in the 'common' package must not depend on feature packages");

  @ArchTest
  static final ArchRule rest_controllers_must_reside_in_item_or_common_package =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("uk.co.hmtt.template.item..")
          .orShould()
          .resideInAPackage("uk.co.hmtt.template.common..")
          .as("@RestController classes must reside in a package ending in .item or .common");
}
