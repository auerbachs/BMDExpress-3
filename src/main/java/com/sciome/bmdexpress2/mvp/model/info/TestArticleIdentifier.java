package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Test article identification with multiple identifier types.
 * Provides various ways to uniquely identify the chemical/compound being tested.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestArticleIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;     // Common/chemical name (e.g., "Perfluoro-3-methoxypropanoic acid")
	private String casrn;    // Chemical Abstracts Service Registry Number (e.g., "13252-13-6")
	private String dsstox;   // DSSTox identifier from NTP/EPA

	/**
	 * Default constructor
	 */
	public TestArticleIdentifier() {
	}

	/**
	 * Constructor with name only (backwards compatibility)
	 */
	public TestArticleIdentifier(String name) {
		this.name = name;
	}

	/**
	 * Constructor with all identifiers
	 */
	public TestArticleIdentifier(String name, String casrn, String dsstox) {
		this.name = name;
		this.casrn = casrn;
		this.dsstox = dsstox;
	}

	// Getters and setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCasrn() {
		return casrn;
	}

	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}

	public String getDsstox() {
		return dsstox;
	}

	public void setDsstox(String dsstox) {
		this.dsstox = dsstox;
	}

	/**
	 * Check if any identifier is populated
	 */
	public boolean hasIdentifier() {
		return (name != null && !name.isEmpty()) ||
		       (casrn != null && !casrn.isEmpty()) ||
		       (dsstox != null && !dsstox.isEmpty());
	}

	/**
	 * Get primary identifier (prefers name, falls back to others)
	 */
	public String getPrimaryIdentifier() {
		if (name != null && !name.isEmpty()) {
			return name;
		}
		if (casrn != null && !casrn.isEmpty()) {
			return "CAS: " + casrn;
		}
		if (dsstox != null && !dsstox.isEmpty()) {
			return "DSSTOX: " + dsstox;
		}
		return null;
	}

	/**
	 * Get formatted string with all available identifiers
	 */
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder();

		if (name != null && !name.isEmpty()) {
			sb.append(name);
		}
		if (casrn != null && !casrn.isEmpty()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append("CAS: ").append(casrn);
		}
		if (dsstox != null && !dsstox.isEmpty()) {
			if (sb.length() > 0) sb.append(" | ");
			sb.append("DSSTOX: ").append(dsstox);
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
