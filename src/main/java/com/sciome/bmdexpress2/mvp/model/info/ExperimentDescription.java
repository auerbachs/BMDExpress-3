package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Experiment description containing all metadata fields.
 * Supports both in vivo and in vitro experiments with different applicable fields.
 *
 * Vocabulary values are loaded from vocabulary.yml via VocabularyConfig.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentDescription implements Serializable {

	private static final long serialVersionUID = 1L;

	// Vocabulary accessors - delegate to VocabularyConfig for externalized values
	public static List<String> getProviderVocabulary() {
		return VocabularyConfig.getInstance().getProviders();
	}

	public static List<String> getPlatformVocabulary() {
		return VocabularyConfig.getInstance().getPlatforms();
	}

	public static List<String> getSubjectTypeVocabulary() {
		return VocabularyConfig.getInstance().getSubjectTypes();
	}

	public static List<String> getArticleRouteVocabulary() {
		return VocabularyConfig.getInstance().getArticleRoutes();
	}

	public static List<String> getArticleVehicleVocabulary() {
		return VocabularyConfig.getInstance().getArticleVehicles();
	}

	public static List<String> getAdministrationMeansVocabulary() {
		return VocabularyConfig.getInstance().getAdministrationMeans();
	}

	public static List<String> getInVitroDurationVocabulary() {
		return VocabularyConfig.getInstance().getInVitroDurations();
	}

	public static List<String> getInVivoDurationVocabulary() {
		return VocabularyConfig.getInstance().getInVivoDurations();
	}

	public static List<String> getStudyDurationVocabulary() {
		return VocabularyConfig.getInstance().getAllDurations();
	}

	public static List<String> getArticleTypeVocabulary() {
		return VocabularyConfig.getInstance().getArticleTypes();
	}

	public static List<String> getSpeciesVocabulary() {
		return VocabularyConfig.getInstance().getSpecies();
	}

	public static List<String> getSexVocabulary() {
		return VocabularyConfig.getInstance().getSexes();
	}

	public static List<String> getOrganVocabulary() {
		return VocabularyConfig.getInstance().getOrgans();
	}

	public static Map<String, List<String>> getStrainsBySpecies() {
		return VocabularyConfig.getInstance().getStrains();
	}

	/**
	 * Get strains for a specific species
	 */
	public static List<String> getStrainsForSpecies(String species) {
		return VocabularyConfig.getInstance().getStrainsForSpecies(species);
	}

	// Legacy constants - delegate to methods for backward compatibility
	// These are kept for existing code that references them directly
	public static final List<String> PROVIDER_VOCABULARY = getProviderVocabulary();
	public static final List<String> PLATFORM_VOCABULARY = getPlatformVocabulary();
	public static final List<String> SUBJECT_TYPE_VOCABULARY = getSubjectTypeVocabulary();
	public static final List<String> ARTICLE_ROUTE_VOCABULARY = getArticleRouteVocabulary();
	public static final List<String> ARTICLE_VEHICLE_VOCABULARY = getArticleVehicleVocabulary();
	public static final List<String> ADMINISTRATION_MEANS_VOCABULARY = getAdministrationMeansVocabulary();
	public static final List<String> IN_VITRO_DURATION_VOCABULARY = getInVitroDurationVocabulary();
	public static final List<String> IN_VIVO_DURATION_VOCABULARY = getInVivoDurationVocabulary();
	public static final List<String> STUDY_DURATION_VOCABULARY = getStudyDurationVocabulary();
	public static final List<String> ARTICLE_TYPE_VOCABULARY = getArticleTypeVocabulary();
	public static final List<String> SPECIES_VOCABULARY = getSpeciesVocabulary();
	public static final List<String> SEX_VOCABULARY = getSexVocabulary();
	public static final List<String> ORGAN_VOCABULARY = getOrganVocabulary();
	public static final Map<String, List<String>> STRAINS_BY_SPECIES = getStrainsBySpecies();

	// Common fields
	private TestArticleIdentifier testArticle;
	private String studyDuration;
	private String platform;
	private String provider;
	private String subjectType;      // "in vivo" or "in vitro"
	private String articleRoute;
	private String articleVehicle;
	private String administrationMeans;
	private String articleType;

	// In vivo specific fields
	private String species;
	private String strain;
	private String sex;
	private String organ;

	// In vitro specific field
	private String cellLine;

	/**
	 * Default constructor
	 */
	public ExperimentDescription() {
	}

	// Getters and setters

	public TestArticleIdentifier getTestArticle() {
		return testArticle;
	}

	public void setTestArticle(TestArticleIdentifier testArticle) {
		this.testArticle = testArticle;
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

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getStrain() {
		return strain;
	}

	public void setStrain(String strain) {
		this.strain = strain;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getOrgan() {
		return organ;
	}

	public void setOrgan(String organ) {
		this.organ = organ;
	}

	public String getCellLine() {
		return cellLine;
	}

	public void setCellLine(String cellLine) {
		this.cellLine = cellLine;
	}

	// Helper methods

	/**
	 * Get the experiment type based on subject type
	 */
	public String getExperimentType() {
		return subjectType != null ? subjectType : "in vivo";
	}

	/**
	 * Check if this is an in vivo experiment
	 */
	public boolean isInVivo() {
		return !"in vitro".equals(subjectType);
	}

	/**
	 * Check if this is an in vitro experiment
	 */
	public boolean isInVitro() {
		return "in vitro".equals(subjectType);
	}

	/**
	 * Check if any description fields are populated
	 */
	public boolean hasDescription() {
		return (testArticle != null && testArticle.hasIdentifier()) ||
		       (studyDuration != null && !studyDuration.isEmpty()) ||
		       (platform != null && !platform.isEmpty()) ||
		       (provider != null && !provider.isEmpty()) ||
		       (subjectType != null && !subjectType.isEmpty()) ||
		       (articleRoute != null && !articleRoute.isEmpty()) ||
		       (articleVehicle != null && !articleVehicle.isEmpty()) ||
		       (administrationMeans != null && !administrationMeans.isEmpty()) ||
		       (articleType != null && !articleType.isEmpty()) ||
		       (species != null && !species.isEmpty()) ||
		       (strain != null && !strain.isEmpty()) ||
		       (sex != null && !sex.isEmpty()) ||
		       (organ != null && !organ.isEmpty()) ||
		       (cellLine != null && !cellLine.isEmpty());
	}

	/**
	 * Get a single-line string suitable for status bar display.
	 * Fields are separated by " | " delimiter.
	 */
	public String getStatusBarString() {
		StringBuilder sb = new StringBuilder();

		if (testArticle != null && testArticle.getPrimaryIdentifier() != null) {
			sb.append(" | Test Article: ").append(testArticle.getPrimaryIdentifier());
		}

		// In vivo fields
		if (species != null && !species.isEmpty()) {
			sb.append(" | Species: ").append(species);
		}
		if (strain != null && !strain.isEmpty()) {
			sb.append(" | Strain: ").append(strain);
		}
		if (sex != null && !sex.isEmpty()) {
			sb.append(" | Sex: ").append(sex);
		}
		if (organ != null && !organ.isEmpty()) {
			sb.append(" | Organ: ").append(organ);
		}

		// In vitro field
		if (cellLine != null && !cellLine.isEmpty()) {
			sb.append(" | Cell Line: ").append(cellLine);
		}

		return sb.toString();
	}

	/**
	 * Get a formatted string representation of the description
	 */
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Experiment Type: ").append(getExperimentType()).append("\n");

		if (testArticle != null && testArticle.hasIdentifier()) {
			sb.append("Test Article: ").append(testArticle.getFormattedString()).append("\n");
		}

		if (subjectType != null && !subjectType.isEmpty()) {
			sb.append("Subject Type: ").append(subjectType).append("\n");
		}

		// In vivo specific fields
		if (species != null && !species.isEmpty()) {
			sb.append("Species: ").append(species).append("\n");
		}
		if (strain != null && !strain.isEmpty()) {
			sb.append("Strain: ").append(strain).append("\n");
		}
		if (sex != null && !sex.isEmpty()) {
			sb.append("Sex: ").append(sex).append("\n");
		}
		if (organ != null && !organ.isEmpty()) {
			sb.append("Organ: ").append(organ).append("\n");
		}

		// In vitro specific field
		if (cellLine != null && !cellLine.isEmpty()) {
			sb.append("Cell Line: ").append(cellLine).append("\n");
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

		return sb.toString();
	}

	/**
	 * Create a copy of this description
	 */
	public ExperimentDescription copy() {
		ExperimentDescription copy = new ExperimentDescription();

		if (testArticle != null) {
			copy.setTestArticle(new TestArticleIdentifier(
				testArticle.getName(),
				testArticle.getCasrn(),
				testArticle.getDsstox()));
		}
		copy.setStudyDuration(studyDuration);
		copy.setPlatform(platform);
		copy.setProvider(provider);
		copy.setSubjectType(subjectType);
		copy.setArticleRoute(articleRoute);
		copy.setArticleVehicle(articleVehicle);
		copy.setAdministrationMeans(administrationMeans);
		copy.setArticleType(articleType);
		copy.setSpecies(species);
		copy.setStrain(strain);
		copy.setSex(sex);
		copy.setOrgan(organ);
		copy.setCellLine(cellLine);

		return copy;
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
