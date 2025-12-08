package com.sciome.bmdexpress2.mvp.model.info.rules;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Base class for rule-based validators using Easy Rules engine.
 *
 * Subclasses should:
 * 1. Override getRules() to return their specific rules
 * 2. Override getFactsKey() to specify the key used in the Facts object
 *
 * @param <F> The type of FactsBase this validator operates on
 */
public abstract class ValidatorBase<F extends FactsBase> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Rules rules = new Rules();
    private final RulesEngine rulesEngine = new DefaultRulesEngine();

    /**
     * Constructor - registers rules on creation
     */
    protected ValidatorBase() {
        registerRules(rules);
        logger.info("Registered {} validation rules for {}", rules.size(), getClass().getSimpleName());
    }

    /**
     * Register all rules. Subclasses must implement this.
     */
    protected abstract void registerRules(Rules rules);

    /**
     * Get the key used to store facts in the Easy Rules Facts object.
     * Default is "facts", but subclasses can override for clarity.
     */
    protected String getFactsKey() {
        return "facts";
    }

    /**
     * Validate facts using the rules engine.
     * After calling this method, check facts.hasErrors() and facts.getErrors()
     * to retrieve validation results.
     *
     * @param facts The facts to validate
     */
    public void validate(F facts) {
        logger.debug("Validating with {} rules", rules.size());

        Facts easyFacts = new Facts();
        easyFacts.put(getFactsKey(), facts);

        rulesEngine.fire(rules, easyFacts);

        if (facts.hasErrors()) {
            logger.debug("Validation found {} errors", facts.getErrors().size());
        } else {
            logger.debug("Validation passed with no errors");
        }

        if (facts.hasWarnings()) {
            logger.debug("Validation found {} warnings", facts.getWarnings().size());
        }
    }

    /**
     * Convenience method to validate and return errors.
     *
     * @param facts The facts to validate
     * @return List of error messages, empty if valid
     */
    public List<String> validateAndGetErrors(F facts) {
        validate(facts);
        return facts.getErrors();
    }

    /**
     * Convenience method to check if facts are valid.
     *
     * @param facts The facts to validate
     * @return true if valid (no errors), false otherwise
     */
    public boolean isValid(F facts) {
        validate(facts);
        return !facts.hasErrors();
    }
}
