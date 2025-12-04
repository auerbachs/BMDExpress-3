package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base class for experiment descriptions.
 * Provides common fields and behavior for both in vivo and in vitro experiments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "experimentType")
@JsonSubTypes({
	@JsonSubTypes.Type(value = InVivoExperimentDescription.class, name = "inVivo"),
	@JsonSubTypes.Type(value = InVitroExperimentDescription.class, name = "inVitro")
})
public abstract class ExperimentDescriptionBase implements Serializable {

	private static final long serialVersionUID = 1L;

	// Common fields for all experiment types
	private TestArticleIdentifier testArticle;  // Test article identification (name, CASRN, DSSTOX)
	private RouteOfAdministrationBase routeOfAdministration;  // How test article was administered
	private String studyDuration;  // Duration of study (e.g., "28 days", "90 days", "2 years")

	/**
	 * Default constructor
	 */
	public ExperimentDescriptionBase() {
	}

	/**
	 * Constructor with common fields
	 */
	public ExperimentDescriptionBase(TestArticleIdentifier testArticle,
	                                  RouteOfAdministrationBase routeOfAdministration,
	                                  String studyDuration) {
		this.testArticle = testArticle;
		this.routeOfAdministration = routeOfAdministration;
		this.studyDuration = studyDuration;
	}

	// Common getters and setters

	public TestArticleIdentifier getTestArticle() {
		return testArticle;
	}

	public void setTestArticle(TestArticleIdentifier testArticle) {
		this.testArticle = testArticle;
	}

	public RouteOfAdministrationBase getRouteOfAdministration() {
		return routeOfAdministration;
	}

	public void setRouteOfAdministration(RouteOfAdministrationBase routeOfAdministration) {
		this.routeOfAdministration = routeOfAdministration;
	}

	public String getStudyDuration() {
		return studyDuration;
	}

	public void setStudyDuration(String studyDuration) {
		this.studyDuration = studyDuration;
	}

	/**
	 * Get the experiment type (in vivo or in vitro)
	 */
	public abstract String getExperimentType();

	/**
	 * Check if any description fields are populated
	 */
	public abstract boolean hasDescription();

	/**
	 * Get a formatted string representation of the description
	 */
	public abstract String getFormattedString();

	/**
	 * Create a copy of this description
	 */
	public abstract ExperimentDescriptionBase copy();

	@Override
	public String toString() {
		return getFormattedString();
	}
}
