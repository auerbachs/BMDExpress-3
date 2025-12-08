package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Experiment description containing all metadata fields.
 * Supports both in vivo and in vitro experiments with different applicable fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentDescription implements Serializable {

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
		"in vivo",
		"in vitro"
	);

	// Test article route vocabulary
	public static final List<String> ARTICLE_ROUTE_VOCABULARY = Arrays.asList(
		"oral",
		"inhaled",
		"transdermal"
	);

	// Test article vehicle vocabulary
	public static final List<String> ARTICLE_VEHICLE_VOCABULARY = Arrays.asList(
		"corn oil",
		"feed",
		"water",
		"aerosol",
		"gas"
	);

	// Test article means of administration vocabulary
	public static final List<String> ADMINISTRATION_MEANS_VOCABULARY = Arrays.asList(
		"gavage",
		"drinking water",
		"dietary"
	);

	// Study duration vocabulary - In Vitro (hours)
	public static final List<String> IN_VITRO_DURATION_VOCABULARY = Arrays.asList(
		"3h",
		"6h",
		"9h",
		"24h"
	);

	// Study duration vocabulary - In Vivo (days)
	public static final List<String> IN_VIVO_DURATION_VOCABULARY = Arrays.asList(
		"1d",
		"3d",
		"5d",
		"7d",
		"14d",
		"28d"
	);

	// Study duration vocabulary - All (for backwards compatibility)
	public static final List<String> STUDY_DURATION_VOCABULARY = Arrays.asList(
		"3h",
		"6h",
		"9h",
		"24h",
		"1d",
		"3d",
		"5d",
		"7d",
		"14d",
		"28d"
	);

	// Test article type vocabulary
	public static final List<String> ARTICLE_TYPE_VOCABULARY = Arrays.asList(
		"chemical",
		"mixture",
		"electromagnetic radiation"
	);

	// Species vocabulary
	public static final List<String> SPECIES_VOCABULARY = Arrays.asList(
		"rat",
		"mouse",
		"human",
		"rabbit",
		"dog",
		"monkey",
		"zebrafish",
		"guinea pig",
		"hamster",
		"pig"
	);

	// Sex vocabulary
	public static final List<String> SEX_VOCABULARY = Arrays.asList(
		"male",
		"female",
		"both",
		"mixed",
		"NA"
	);

	// Organ vocabulary
	public static final List<String> ORGAN_VOCABULARY = Arrays.asList(
		"adrenal",
		"blood",
		"bone",
		"brain",
		"colon",
		"heart",
		"intestine",
		"kidney",
		"liver",
		"lung",
		"muscle",
		"ovary",
		"pancreas",
		"prostate",
		"skin",
		"spleen",
		"stomach",
		"testes",
		"thymus",
		"thyroid",
		"uterus"
	);

	// Strains by species
	public static final Map<String, List<String>> STRAINS_BY_SPECIES = createStrainMap();

	private static Map<String, List<String>> createStrainMap() {
		Map<String, List<String>> map = new HashMap<>();
		map.put("rat", Arrays.asList(
			"Sprague-Dawley", "Wistar", "Long-Evans", "Fischer 344", "Brown Norway"
		));
		map.put("mouse", Arrays.asList(
			"C57BL/6", "BALB/c", "CD-1", "FVB/N", "129", "DBA/2", "NOD", "SCID"
		));
		return map;
	}

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

	/**
	 * Get list of strains for a given species
	 */
	public static List<String> getStrainsForSpecies(String species) {
		if (species == null || !STRAINS_BY_SPECIES.containsKey(species)) {
			return Arrays.asList();
		}
		return STRAINS_BY_SPECIES.get(species);
	}

	@Override
	public String toString() {
		return getFormattedString();
	}
}
