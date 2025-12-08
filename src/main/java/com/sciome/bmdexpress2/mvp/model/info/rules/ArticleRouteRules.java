package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rules for Article Route dependencies.
 *
 * Dependencies:
 * - Oral → allows Administration Means (Gavage, Drinking Water, Dietary)
 * - Oral → allows Article Vehicle (Corn Oil, Feed, Water)
 * - Inhaled → allows Article Vehicle (Aerosol, Gas), NO Administration Means
 * - Transdermal → NO Article Vehicle, NO Administration Means
 * - Electromagnetic Radiation → NO Article Vehicle
 */
public class ArticleRouteRules {

    public static final List<String> ORAL_VEHICLES = Arrays.asList("corn oil", "feed", "water");
    public static final List<String> INHALED_VEHICLES = Arrays.asList("aerosol", "gas");
    public static final List<String> ADMINISTRATION_MEANS = Arrays.asList("gavage", "drinking water", "dietary");

    // ===== Administration Means Rules =====

    @Rule(name = "AdminMeansOnlyForOralRule",
          description = "Administration Means is only valid for Oral route",
          priority = 1)
    public static class AdminMeansOnlyForOral {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return !facts.isOralRoute()
                && facts.hasValue(facts.getAdministrationMeans());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Administration Means '%s' is not valid for %s route. Administration Means only applies to Oral route.",
                facts.getAdministrationMeans(),
                facts.getArticleRoute() != null ? facts.getArticleRoute() : "non-Oral"));
        }
    }

    @Rule(name = "OralRequiresAdminMeansRule",
          description = "Oral route requires Administration Means",
          priority = 2)
    public static class OralRequiresAdminMeans {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isOralRoute() && !facts.hasValue(facts.getAdministrationMeans());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("Oral route requires Administration Means: " + ADMINISTRATION_MEANS);
            facts.setValidOptions("administrationMeans", ADMINISTRATION_MEANS);
        }
    }

    // ===== Article Vehicle Rules =====

    @Rule(name = "TransdermalNoVehicleRule",
          description = "Transdermal route should not have Article Vehicle",
          priority = 1)
    public static class TransdermalNoVehicle {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isTransdermalRoute() && facts.hasValue(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not valid for Transdermal route. Transdermal does not use Article Vehicle.",
                facts.getArticleVehicle()));
        }
    }

    @Rule(name = "EMFNoVehicleRule",
          description = "Electromagnetic Radiation should not have Article Vehicle",
          priority = 1)
    public static class EMFNoVehicle {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isElectromagneticRadiation() && facts.hasValue(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not valid for Electromagnetic Radiation. EMF does not use Article Vehicle.",
                facts.getArticleVehicle()));
        }
    }

    @Rule(name = "OralVehicleValidationRule",
          description = "Oral route requires valid vehicle",
          priority = 2)
    public static class OralVehicleValidation {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isOralRoute()
                && facts.hasValue(facts.getArticleVehicle())
                && !ORAL_VEHICLES.contains(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not valid for Oral route. Must be one of: %s",
                facts.getArticleVehicle(), ORAL_VEHICLES));
            facts.setValidOptions("articleVehicle", ORAL_VEHICLES);
        }
    }

    @Rule(name = "InhaledVehicleValidationRule",
          description = "Inhaled route requires valid vehicle",
          priority = 2)
    public static class InhaledVehicleValidation {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInhaledRoute()
                && facts.hasValue(facts.getArticleVehicle())
                && !INHALED_VEHICLES.contains(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not valid for Inhaled route. Must be one of: %s",
                facts.getArticleVehicle(), INHALED_VEHICLES));
            facts.setValidOptions("articleVehicle", INHALED_VEHICLES);
        }
    }

    // ===== Vehicle <-> Means Consistency Rules =====

    @Rule(name = "GavageVehicleConsistencyRule",
          description = "Gavage requires Corn Oil or Water vehicle",
          priority = 3)
    public static class GavageVehicleConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            // Only check when route is Oral (Administration Means is applicable)
            return facts.isOralRoute()
                && "gavage".equals(facts.getAdministrationMeans())
                && facts.hasValue(facts.getArticleVehicle())
                && !"corn oil".equals(facts.getArticleVehicle())
                && !"water".equals(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not compatible with gavage. Use 'corn oil' or 'water'.",
                facts.getArticleVehicle()));
        }
    }

    @Rule(name = "DrinkingWaterVehicleConsistencyRule",
          description = "Drinking Water means requires Water vehicle",
          priority = 3)
    public static class DrinkingWaterVehicleConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            // Only check when route is Oral (Administration Means is applicable)
            return facts.isOralRoute()
                && "drinking water".equals(facts.getAdministrationMeans())
                && facts.hasValue(facts.getArticleVehicle())
                && !"water".equals(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not compatible with drinking water. Must use 'water'.",
                facts.getArticleVehicle()));
        }
    }

    @Rule(name = "DietaryVehicleConsistencyRule",
          description = "Dietary means requires Feed vehicle",
          priority = 3)
    public static class DietaryVehicleConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            // Only check when route is Oral (Administration Means is applicable)
            return facts.isOralRoute()
                && "dietary".equals(facts.getAdministrationMeans())
                && facts.hasValue(facts.getArticleVehicle())
                && !"feed".equals(facts.getArticleVehicle());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "Article Vehicle '%s' is not compatible with dietary. Must use 'feed'.",
                facts.getArticleVehicle()));
        }
    }
}
