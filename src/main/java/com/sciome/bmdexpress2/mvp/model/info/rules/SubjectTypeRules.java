package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.Arrays;
import java.util.List;

/**
 * Rules for Subject Type dependencies.
 *
 * Dependencies:
 * - In Vitro → requires cellLine, study duration in hours (3h, 6h, 9h, 24h)
 * - In Vivo → requires species/strain/sex/organ, study duration in days (1d, 3d, 5d, 7d, 14d, 28d)
 */
public class SubjectTypeRules {

    public static final List<String> IN_VITRO_DURATIONS = Arrays.asList("3h", "6h", "9h", "24h");
    public static final List<String> IN_VIVO_DURATIONS = Arrays.asList("1d", "3d", "5d", "7d", "14d", "28d");

    @Rule(name = "InVitroDurationRule",
          description = "In Vitro experiments must use hour-based durations",
          priority = 1)
    public static class InVitroDurationRule {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVitro()
                && facts.hasValue(facts.getStudyDuration())
                && !IN_VITRO_DURATIONS.contains(facts.getStudyDuration());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "In Vitro study duration '%s' is invalid. Must be one of: %s",
                facts.getStudyDuration(), IN_VITRO_DURATIONS));
            facts.setValidOptions("studyDuration", IN_VITRO_DURATIONS);
        }
    }

    @Rule(name = "InVivoDurationRule",
          description = "In Vivo experiments must use day-based durations",
          priority = 1)
    public static class InVivoDurationRule {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVivo()
                && facts.hasValue(facts.getStudyDuration())
                && !IN_VIVO_DURATIONS.contains(facts.getStudyDuration());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError(String.format(
                "In Vivo study duration '%s' is invalid. Must be one of: %s",
                facts.getStudyDuration(), IN_VIVO_DURATIONS));
            facts.setValidOptions("studyDuration", IN_VIVO_DURATIONS);
        }
    }

    @Rule(name = "InVitroNoCellLineRule",
          description = "In Vitro experiments require a cell line",
          priority = 2)
    public static class InVitroRequiresCellLine {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVitro() && !facts.hasValue(facts.getCellLine());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("In Vitro experiments require a Cell Line");
        }
    }

    @Rule(name = "InVivoRequiresSpeciesRule",
          description = "In Vivo experiments require species",
          priority = 2)
    public static class InVivoRequiresSpecies {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVivo() && !facts.hasValue(facts.getSpecies());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("In Vivo experiments require Species");
        }
    }

    @Rule(name = "InVivoRequiresStrainRule",
          description = "In Vivo experiments require strain",
          priority = 2)
    public static class InVivoRequiresStrain {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVivo() && !facts.hasValue(facts.getStrain());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("In Vivo experiments require Strain");
        }
    }

    @Rule(name = "InVivoRequiresSexRule",
          description = "In Vivo experiments require sex",
          priority = 2)
    public static class InVivoRequiresSex {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVivo() && !facts.hasValue(facts.getSex());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("In Vivo experiments require Sex");
        }
    }

    @Rule(name = "InVivoRequiresOrganRule",
          description = "In Vivo experiments require organ",
          priority = 2)
    public static class InVivoRequiresOrgan {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVivo() && !facts.hasValue(facts.getOrgan());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addError("In Vivo experiments require Organ");
        }
    }

    @Rule(name = "InVitroCannotHaveSpeciesRule",
          description = "In Vitro experiments should not have species/strain/sex/organ",
          priority = 3)
    public static class InVitroCannotHaveInVivoFields {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            return facts.isInVitro() && (
                facts.hasValue(facts.getSpecies()) ||
                facts.hasValue(facts.getStrain()) ||
                facts.hasValue(facts.getSex()) ||
                facts.hasValue(facts.getOrgan())
            );
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            facts.addWarning("In Vitro experiments should not have Species, Strain, Sex, or Organ fields");
        }
    }
}
