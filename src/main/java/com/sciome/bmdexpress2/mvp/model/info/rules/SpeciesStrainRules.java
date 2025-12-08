package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;

import java.util.*;

/**
 * Rules for Species and Strain dependencies.
 *
 * Dependencies:
 * - Species → Strain (only matching strains for the species)
 */
public class SpeciesStrainRules {

    // Species → Strain mapping
    private static final Map<String, List<String>> SPECIES_STRAINS = new HashMap<>();

    static {
        SPECIES_STRAINS.put("rat", Arrays.asList(
            "Sprague-Dawley", "Fischer 344", "Wistar", "Long-Evans", "Brown Norway"
        ));
        SPECIES_STRAINS.put("mouse", Arrays.asList(
            "C57BL/6", "BALB/c", "CD-1", "FVB/N", "129S1/SvImJ", "DBA/2"
        ));
        SPECIES_STRAINS.put("human", Collections.emptyList()); // No strain for human
        SPECIES_STRAINS.put("rabbit", Arrays.asList("New Zealand White", "Dutch"));
        SPECIES_STRAINS.put("dog", Arrays.asList("Beagle"));
        SPECIES_STRAINS.put("monkey", Arrays.asList("Cynomolgus", "Rhesus"));
        SPECIES_STRAINS.put("zebrafish", Arrays.asList("AB", "TU", "WIK"));
        SPECIES_STRAINS.put("guinea pig", Arrays.asList("Hartley", "Dunkin-Hartley"));
        SPECIES_STRAINS.put("hamster", Arrays.asList("Syrian", "Chinese"));
        SPECIES_STRAINS.put("pig", Arrays.asList("Yorkshire", "Landrace", "Duroc"));
    }

    public static List<String> getStrainsForSpecies(String species) {
        return SPECIES_STRAINS.getOrDefault(species, Collections.emptyList());
    }

    public static Set<String> getAllSpecies() {
        return SPECIES_STRAINS.keySet();
    }

    @Rule(name = "StrainSpeciesConsistencyRule",
          description = "Strain must be valid for the selected species",
          priority = 1)
    public static class StrainSpeciesConsistency {

        @Condition
        public boolean when(@Fact("metadata") MetadataFacts facts) {
            if (!facts.hasValue(facts.getSpecies()) || !facts.hasValue(facts.getStrain())) {
                return false;
            }

            List<String> validStrains = getStrainsForSpecies(facts.getSpecies());
            if (validStrains.isEmpty()) {
                // No strain restrictions for this species
                return false;
            }

            return !validStrains.contains(facts.getStrain());
        }

        @Action
        public void then(@Fact("metadata") MetadataFacts facts) {
            List<String> validStrains = getStrainsForSpecies(facts.getSpecies());
            facts.addError(String.format(
                "Strain '%s' is not valid for species '%s'. Valid strains: %s",
                facts.getStrain(), facts.getSpecies(), validStrains));
            facts.setValidOptions("strain", validStrains);
        }
    }
}
