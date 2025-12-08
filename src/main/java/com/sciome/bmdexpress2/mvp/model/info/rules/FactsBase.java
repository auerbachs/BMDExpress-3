package com.sciome.bmdexpress2.mvp.model.info.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for rule-based validation facts containers.
 * Provides common error/warning collection and utility methods.
 *
 * Subclasses should add domain-specific fields and builder methods.
 */
public abstract class FactsBase {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<String, List<String>> validOptions = new HashMap<>();

    /**
     * Add an error message
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Add a warning message
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Set valid options for a field (for UI hints)
     */
    public void setValidOptions(String fieldName, List<String> options) {
        validOptions.put(fieldName, options);
    }

    /**
     * Get all error messages (returns a copy)
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Get all warning messages (returns a copy)
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * Get valid options map (returns a copy)
     */
    public Map<String, List<String>> getValidOptions() {
        return new HashMap<>(validOptions);
    }

    /**
     * Check if there are any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are any warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Utility method to check if a string value is present and not empty.
     * Also treats "NA" (case-insensitive) as not having a value.
     */
    public boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty() && !"NA".equalsIgnoreCase(value.trim());
    }
}
