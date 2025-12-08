package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.api.Rules;

/**
 * Metadata validator using Easy Rules engine.
 *
 * This class orchestrates the validation of experiment metadata using
 * a rule-based approach. Rules are defined as separate classes and
 * can be easily added, modified, or removed.
 *
 * Usage:
 * <pre>
 * MetadataFacts facts = new MetadataFacts()
 *     .subjectType("in vivo")
 *     .species("rat")
 *     .strain("C57BL/6")  // Wrong! This is a mouse strain
 *     .articleRoute("oral")
 *     .administrationMeans("gavage");
 *
 * MetadataValidator.validateMetadata(facts);
 *
 * if (facts.hasErrors()) {
 *     for (String error : facts.getErrors()) {
 *         System.out.println("Error: " + error);
 *     }
 * }
 * </pre>
 */
public class MetadataValidator extends ValidatorBase<MetadataFacts> {

    // Singleton instance for static access
    private static final MetadataValidator INSTANCE = new MetadataValidator();

    public MetadataValidator() {
        super();
    }

    @Override
    protected void registerRules(Rules rules) {
        // Subject Type rules
        rules.register(new SubjectTypeRules.InVitroDurationRule());
        rules.register(new SubjectTypeRules.InVivoDurationRule());
        rules.register(new SubjectTypeRules.InVitroRequiresCellLine());
        rules.register(new SubjectTypeRules.InVivoRequiresSpecies());
        rules.register(new SubjectTypeRules.InVivoRequiresStrain());
        rules.register(new SubjectTypeRules.InVivoRequiresSex());
        rules.register(new SubjectTypeRules.InVivoRequiresOrgan());
        rules.register(new SubjectTypeRules.InVitroCannotHaveInVivoFields());

        // Article Route rules
        rules.register(new ArticleRouteRules.AdminMeansOnlyForOral());
        rules.register(new ArticleRouteRules.OralRequiresAdminMeans());
        rules.register(new ArticleRouteRules.TransdermalNoVehicle());
        rules.register(new ArticleRouteRules.EMFNoVehicle());
        rules.register(new ArticleRouteRules.OralVehicleValidation());
        rules.register(new ArticleRouteRules.InhaledVehicleValidation());
        rules.register(new ArticleRouteRules.GavageVehicleConsistency());
        rules.register(new ArticleRouteRules.DrinkingWaterVehicleConsistency());
        rules.register(new ArticleRouteRules.DietaryVehicleConsistency());

        // Species/Strain rules
        rules.register(new SpeciesStrainRules.StrainSpeciesConsistency());

        // Platform rules
        rules.register(new PlatformRules.PlatformProviderConsistency());
        rules.register(new PlatformRules.PlatformSpeciesConsistency());
    }

    @Override
    protected String getFactsKey() {
        return "metadata";
    }

    // Static convenience methods that delegate to singleton instance

    /**
     * Validate metadata facts using the rules engine.
     * After calling this method, check facts.hasErrors() and facts.getErrors()
     * to retrieve validation results.
     *
     * @param facts The metadata facts to validate
     */
    public static void validateMetadata(MetadataFacts facts) {
        INSTANCE.validate(facts);
    }

    /**
     * Convenience method to validate and return errors.
     *
     * @param facts The metadata facts to validate
     * @return List of error messages, empty if valid
     */
    public static java.util.List<String> getMetadataErrors(MetadataFacts facts) {
        return INSTANCE.validateAndGetErrors(facts);
    }

    /**
     * Convenience method to check if metadata is valid.
     *
     * @param facts The metadata facts to validate
     * @return true if valid (no errors), false otherwise
     */
    public static boolean isMetadataValid(MetadataFacts facts) {
        return INSTANCE.isValid(facts);
    }
}
