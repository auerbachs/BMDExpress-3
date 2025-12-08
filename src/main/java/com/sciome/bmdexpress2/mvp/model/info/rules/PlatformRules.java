package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

/**
 * Rules for Platform dependencies.
 *
 * Dependencies:
 * - Platform → Provider (Affymetrix platforms → Affymetrix provider, etc.)
 * - Platform → Species (Rat platforms → Rat species, etc.)
 */
public class PlatformRules {

    /**
     * Infer provider from platform name.
     */
    public static String inferProvider(String platform) {
        if (platform == null) return null;

        if (platform.startsWith("Affymetrix")) return "Affymetrix";
        if (platform.startsWith("Agilent")) return "Agilent";
        if (platform.startsWith("BioSpyder")) return "BioSpyder";
        if (platform.startsWith("Ensembl")) return "Ensembl";
        if (platform.startsWith("RefSeq")) return "RefSeq";
        if (platform.contains("Clinical") || platform.contains("Hematology") ||
            platform.contains("Organ Weight")) return "Clinical Endpoint";
        if (platform.equals("Generic")) return "Generic";

        return null;
    }

    /**
     * Extract species from platform name.
     */
    public static String inferSpecies(String platform) {
        if (platform == null) return null;

        String lower = platform.toLowerCase();

        if (lower.contains("rat") || lower.contains("rn6")) return "rat";
        if (lower.contains("mouse") || lower.contains("murine") || lower.contains("mm10")) return "mouse";
        if (lower.contains("human") || lower.contains("hg19")) return "human";
        if (lower.contains("drosophila")) return "drosophila";
        if (lower.contains("zebrafish")) return "zebrafish";

        return null;
    }

    @Rule(name = "PlatformProviderConsistencyRule",
          description = "Provider must be consistent with Platform",
          priority = 1)
    public static class PlatformProviderConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            if (!facts.hasValue(facts.getPlatform()) || !facts.hasValue(facts.getProvider())) {
                return false;
            }

            String inferredProvider = inferProvider(facts.getPlatform());
            if (inferredProvider == null) {
                return false; // Can't infer, so can't validate
            }

            return !inferredProvider.equalsIgnoreCase(facts.getProvider());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            String inferredProvider = inferProvider(facts.getPlatform());
            facts.addError(String.format(
                "Provider '%s' is not consistent with Platform '%s'. Expected provider: '%s'",
                facts.getProvider(), facts.getPlatform(), inferredProvider));
        }
    }

    @Rule(name = "PlatformSpeciesConsistencyRule",
          description = "Species must be consistent with Platform",
          priority = 1)
    public static class PlatformSpeciesConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            if (!facts.hasValue(facts.getPlatform()) || !facts.hasValue(facts.getSpecies())) {
                return false;
            }

            String inferredSpecies = inferSpecies(facts.getPlatform());
            if (inferredSpecies == null) {
                return false; // Can't infer, so can't validate
            }

            return !inferredSpecies.equalsIgnoreCase(facts.getSpecies());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            String inferredSpecies = inferSpecies(facts.getPlatform());
            facts.addError(String.format(
                "Species '%s' is not consistent with Platform '%s'. Platform is for species: '%s'",
                facts.getSpecies(), facts.getPlatform(), inferredSpecies));
        }
    }
}
