package com.sciome.bmdexpress2.mvp.model.info;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * In vivo experiment description with animal-specific fields.
 * Includes species, strain, sex, and organ information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InVivoExperimentDescription extends ExperimentDescriptionBase {

	private static final long serialVersionUID = 1L;

	// Controlled vocabularies for standardization
	public static final List<String> SPECIES_VOCABULARY = Arrays.asList(
		"Rat",
		"Mouse",
		"Human",
		"Rabbit",
		"Dog",
		"Monkey",
		"Zebrafish",
		"Guinea Pig",
		"Hamster",
		"Pig"
	);

	public static final List<String> SEX_VOCABULARY = Arrays.asList(
		"Male",
		"Female",
		"Both",
		"Mixed",
		"NA"
	);

	public static final List<String> ORGAN_VOCABULARY = Arrays.asList(
		"Adrenal",
		"Blood",
		"Bone",
		"Brain",
		"Colon",
		"Heart",
		"Intestine",
		"Kidney",
		"Liver",
		"Lung",
		"Muscle",
		"Ovary",
		"Pancreas",
		"Prostate",
		"Skin",
		"Spleen",
		"Stomach",
		"Testes",
		"Thymus",
		"Thyroid",
		"Uterus"
	);

	public static final Map<String, List<String>> STRAINS_BY_SPECIES = createStrainMap();

	private static Map<String, List<String>> createStrainMap() {
		Map<String, List<String>> map = new HashMap<>();
		map.put("Rat", Arrays.asList(
			"Sprague-Dawley",
			"Wistar",
			"Long-Evans",
			"Fischer 344",
			"Brown Norway"
		));
		map.put("Mouse", Arrays.asList(
			"C57BL/6",
			"BALB/c",
			"CD-1",
			"FVB/N",
			"129",
			"DBA/2",
			"NOD",
			"SCID"
		));
		map.put("Human", Arrays.asList());
		map.put("Rabbit", Arrays.asList());
		map.put("Dog", Arrays.asList());
		map.put("Monkey", Arrays.asList());
		map.put("Zebrafish", Arrays.asList());
		map.put("Guinea Pig", Arrays.asList());
		map.put("Hamster", Arrays.asList());
		map.put("Pig", Arrays.asList());
		return map;
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

	// In vivo specific fields
	private String species;  // Animal species (e.g., "Rat", "Mouse")
	private String strain;   // Animal strain (e.g., "Sprague-Dawley", "C57BL/6")
	private String sex;      // Sex (e.g., "Male", "Female", "Both", "Mixed")
	private String organ;    // Target organ/tissue (e.g., "Liver", "Kidney")

	/**
	 * Default constructor
	 */
	public InVivoExperimentDescription() {
		super();
	}

	/**
	 * Constructor with all fields
	 */
	public InVivoExperimentDescription(TestArticleIdentifier testArticle,
	                                    RouteOfAdministrationBase routeOfAdministration,
	                                    String studyDuration,
	                                    String species,
	                                    String strain,
	                                    String sex,
	                                    String organ) {
		super(testArticle, routeOfAdministration, studyDuration);
		this.species = species;
		this.strain = strain;
		this.sex = sex;
		this.organ = organ;
	}

	// Getters and setters

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

	@Override
	public String getExperimentType() {
		return "In Vivo";
	}

	@Override
	public boolean hasDescription() {
		return hasBaseDescription() ||
		       (species != null && !species.isEmpty()) ||
		       (strain != null && !strain.isEmpty()) ||
		       (sex != null && !sex.isEmpty()) ||
		       (organ != null && !organ.isEmpty());
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Experiment Type: In Vivo\n");

		// Append base fields using helper method
		appendBaseFormattedString(sb);

		// Append InVivo-specific fields
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

		return sb.toString();
	}

	@Override
	public ExperimentDescriptionBase copy() {
		InVivoExperimentDescription copy = new InVivoExperimentDescription();

		// Copy base fields using helper method
		copyBaseFields(copy);

		// Copy InVivo-specific fields
		copy.setSpecies(species);
		copy.setStrain(strain);
		copy.setSex(sex);
		copy.setOrgan(organ);

		return copy;
	}
}
