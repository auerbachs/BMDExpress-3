package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

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

	// Controlled vocabularies for platform providers
	public static final List<String> PROVIDER_VOCABULARY = Arrays.asList(
		"Affymetrix",
		"Agilent",
		"BioSpyder",
		"RefSeq",
		"Ensembl",
		"Clinical Endpoint",
		"Generic"
	);

	// Controlled vocabulary for array platforms
	public static final List<String> PLATFORM_VOCABULARY = Arrays.asList(
		// Affymetrix platforms
		"Affymetrix Drosophila Genome Array",
		"Affymetrix Drosophila Genome 2.0 Array",
		"Affymetrix Human HG-Focus Target Array",
		"Affymetrix Human Genome U133A Array",
		"Affymetrix Human Genome U133 Plus 2.0 Array",
		"Affymetrix Human Genome U133A 2.0 Array",
		"Affymetrix HT HG-U133+ PM Array Plate",
		"Affymetrix HT MG-430 PM Array Plate",
		"Affymetrix HT RG-230 PM Array Plate",
		"Affymetrix Murine Genome U74A Array",
		"Affymetrix Murine Genome U74A Version 2 Array",
		"Affymetrix Mouse Expression 430A Array",
		"Affymetrix Mouse Expression 430B Array",
		"Affymetrix Mouse Genome 430A 2.0 Array",
		"Affymetrix Mouse Genome 430 2.0 Array",
		"Affymetrix Rat Expression 230A Array",
		"Affymetrix Rat Expression 230B Array",
		"Affymetrix Rat Genome 230 2.0 Array",
		"Affymetrix Rat Genome U34 Array",
		"Affymetrix Zebrafish Genome Array",
		// Agilent platforms
		"Agilent Drosophila Oligo Microarray",
		"Agilent Whole Human Genome Microarray 4x44K",
		"Agilent Whole Mouse Genome Microarray 4x44K",
		"Agilent Whole Rat Genome Microarray 4x44K",
		"Agilent Zebrafish Oligo Microarray",
		// BioSpyder platforms
		"BioSpyder S1500+ Rat",
		"BioSpyder S1500+ Human",
		// Genomic reference platforms
		"Ensembl hg19",
		"Ensembl mm10",
		"Ensembl rn6",
		"RefSeq hg19",
		"RefSeq mm10",
		"RefSeq rn6",
		// Clinical endpoints
		"Clinical Chemistry",
		"Hematology",
		"Organ Weight",
		"Generic"
	);

	// Test subject type vocabulary
	public static final List<String> SUBJECT_TYPE_VOCABULARY = Arrays.asList(
		"In Vivo",
		"In Vitro"
	);

	// Test article route vocabulary
	public static final List<String> ARTICLE_ROUTE_VOCABULARY = Arrays.asList(
		"Oral",
		"Inhaled",
		"Transdermal"
	);

	// Test article vehicle vocabulary
	public static final List<String> ARTICLE_VEHICLE_VOCABULARY = Arrays.asList(
		"Corn Oil",
		"Feed",
		"Water",
		"Aerosol",
		"Gas"
	);

	// Test article means of administration vocabulary
	public static final List<String> ADMINISTRATION_MEANS_VOCABULARY = Arrays.asList(
		"Gavage",
		"Drinking Water",
		"Dietary"
	);

	// Study duration vocabulary
	public static final List<String> STUDY_DURATION_VOCABULARY = Arrays.asList(
		"5d",
		"24h",
		"28d",
		"90d"
	);

	// Test article type vocabulary
	public static final List<String> ARTICLE_TYPE_VOCABULARY = Arrays.asList(
		"Chemical",
		"Mixture",
		"Electromagnetic Radiation"
	);

	// Common fields for all experiment types
	private TestArticleIdentifier testArticle;  // Test article identification (name, CASRN, DSSTOX)
	private RouteOfAdministrationBase routeOfAdministration;  // How test article was administered
	private String studyDuration;  // Duration of study (e.g., "28d", "90d", "5d")
	private String platform;  // Array platform/chip name (e.g., "Rat Genome 230 2.0 Array")
	private String provider;  // Platform provider (e.g., "Affymetrix", "Agilent", "BioSpyder")

	// New simplified metadata fields
	private String subjectType;  // Test subject type: "In Vivo" or "In Vitro"
	private String articleRoute;  // Test article route: "Oral", "Inhaled", "Transdermal"
	private String articleVehicle;  // Test article vehicle: "Corn Oil", "Feed", "Water", etc.
	private String administrationMeans;  // Means of administration: "Gavage", "Drinking Water", "Dietary"
	private String articleType;  // Test article type: "Chemical", "Mixture", "Electromagnetic Radiation"

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

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

	public String getArticleRoute() {
		return articleRoute;
	}

	public void setArticleRoute(String articleRoute) {
		this.articleRoute = articleRoute;
	}

	public String getArticleVehicle() {
		return articleVehicle;
	}

	public void setArticleVehicle(String articleVehicle) {
		this.articleVehicle = articleVehicle;
	}

	public String getAdministrationMeans() {
		return administrationMeans;
	}

	public void setAdministrationMeans(String administrationMeans) {
		this.administrationMeans = administrationMeans;
	}

	public String getArticleType() {
		return articleType;
	}

	public void setArticleType(String articleType) {
		this.articleType = articleType;
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
	 * Check if any base (common) description fields are populated
	 * This method is called by subclasses to avoid code duplication
	 */
	protected boolean hasBaseDescription() {
		return (testArticle != null && testArticle.hasIdentifier()) ||
		       (routeOfAdministration != null) ||
		       (studyDuration != null && !studyDuration.isEmpty()) ||
		       (platform != null && !platform.isEmpty()) ||
		       (provider != null && !provider.isEmpty()) ||
		       (subjectType != null && !subjectType.isEmpty()) ||
		       (articleRoute != null && !articleRoute.isEmpty()) ||
		       (articleVehicle != null && !articleVehicle.isEmpty()) ||
		       (administrationMeans != null && !administrationMeans.isEmpty()) ||
		       (articleType != null && !articleType.isEmpty());
	}

	/**
	 * Get a formatted string representation of the description
	 */
	public abstract String getFormattedString();

	/**
	 * Append base (common) fields to formatted string
	 * This method is called by subclasses to avoid code duplication
	 */
	protected void appendBaseFormattedString(StringBuilder sb) {
		if (testArticle != null && testArticle.hasIdentifier()) {
			sb.append("Test Article: ").append(testArticle.getFormattedString()).append("\n");
		}

		if (subjectType != null && !subjectType.isEmpty()) {
			sb.append("Subject Type: ").append(subjectType).append("\n");
		}

		if (articleRoute != null && !articleRoute.isEmpty()) {
			sb.append("Article Route: ").append(articleRoute).append("\n");
		}

		if (articleVehicle != null && !articleVehicle.isEmpty()) {
			sb.append("Article Vehicle: ").append(articleVehicle).append("\n");
		}

		if (administrationMeans != null && !administrationMeans.isEmpty()) {
			sb.append("Administration Means: ").append(administrationMeans).append("\n");
		}

		if (routeOfAdministration != null) {
			sb.append("Route of Administration: ").append(routeOfAdministration.getFormattedString()).append("\n");
		}

		if (studyDuration != null && !studyDuration.isEmpty()) {
			sb.append("Study Duration: ").append(studyDuration).append("\n");
		}

		if (platform != null && !platform.isEmpty()) {
			sb.append("Platform: ").append(platform).append("\n");
		}

		if (provider != null && !provider.isEmpty()) {
			sb.append("Provider: ").append(provider).append("\n");
		}

		if (articleType != null && !articleType.isEmpty()) {
			sb.append("Article Type: ").append(articleType).append("\n");
		}
	}

	/**
	 * Create a copy of this description
	 */
	public abstract ExperimentDescriptionBase copy();

	/**
	 * Copy base (common) fields to target description
	 * This method is called by subclasses to avoid code duplication
	 */
	protected void copyBaseFields(ExperimentDescriptionBase target) {
		if (testArticle != null) {
			target.setTestArticle(new TestArticleIdentifier(
				testArticle.getName(),
				testArticle.getCasrn(),
				testArticle.getDsstox()));
		}
		target.setRouteOfAdministration(routeOfAdministration);  // Routes are immutable
		target.setStudyDuration(studyDuration);
		target.setPlatform(platform);
		target.setProvider(provider);
		target.setSubjectType(subjectType);
		target.setArticleRoute(articleRoute);
		target.setArticleVehicle(articleVehicle);
		target.setAdministrationMeans(administrationMeans);
		target.setArticleType(articleType);
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
