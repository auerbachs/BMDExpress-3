package com.sciome.bmdexpress2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescriptionBase;
import com.sciome.bmdexpress2.mvp.model.info.InVivoExperimentDescription;
import com.sciome.bmdexpress2.mvp.model.info.InVitroExperimentDescription;
import com.sciome.bmdexpress2.mvp.model.info.TestArticleIdentifier;
import com.sciome.bmdexpress2.mvp.model.info.RouteOfAdministrationBase;
import com.sciome.bmdexpress2.mvp.model.info.InhalationRoute;
import com.sciome.bmdexpress2.mvp.model.info.OralRoute;
import com.sciome.bmdexpress2.mvp.model.info.TransdermalRoute;
import com.sciome.bmdexpress2.mvp.model.info.IntravenousRoute;

/**
 * Utility class to parse experimental metadata from filenames and file headers.
 *
 * Example filename patterns:
 * - P2_Perfluoro_3_methoxypropanoic_acid_Male_Thyroid-expression1.txt
 * - PFAS_Female_Liver.csv
 * - Rat_SpragueDawley_Male_Kidney_ToxicantX.dat
 *
 * Example file header format:
 * # BMDExpress Metadata
 * # Species: Rat
 * # Strain: Sprague-Dawley
 * # Sex: Male
 * # Organ: Liver
 * # Test Article: PFOA
 * # CASRN: 335-67-1
 * # Route: Oral (Gavage)
 * # Vehicle: Corn Oil
 * # Study Duration: 28 days
 */
public class ExperimentDescriptionParser {

	// Common sex keywords
	private static final Set<String> SEX_KEYWORDS = new HashSet<>(Arrays.asList(
		"male", "female", "both", "mixed", "m", "f"
	));

	// Common organ keywords
	private static final Set<String> ORGAN_KEYWORDS = new HashSet<>(Arrays.asList(
		"liver", "kidney", "heart", "brain", "lung", "spleen", "thyroid", "thymus",
		"adrenal", "ovary", "uterus", "testes", "testis", "pancreas", "stomach",
		"intestine", "colon", "skin", "muscle", "bone", "blood", "plasma", "serum"
	));

	// Common species keywords
	private static final Set<String> SPECIES_KEYWORDS = new HashSet<>(Arrays.asList(
		"rat", "mouse", "human", "dog", "rabbit", "monkey", "pig", "hamster"
	));

	// Common strain keywords
	private static final Set<String> STRAIN_KEYWORDS = new HashSet<>(Arrays.asList(
		"spraguedawley", "sd", "fischer", "wistar", "c57bl6", "balb", "cd1"
	));

	/**
	 * Parse file to extract experimental metadata from both header and filename
	 *
	 * @param file The input file
	 * @return ParseResult containing the parsed description and any validation issues
	 */
	public static ParseResult parseFromFile(File file) {
		if (file == null) {
			return new ParseResult(new InVivoExperimentDescription(), new ArrayList<>());
		}

		// Parse file header only - no filename fallback
		ParseResult headerResult = parseFileHeader(file);

		// DISABLED: Filename parsing fallback
		// If no header metadata found, return empty description (user must fill manually)
		// if (!headerResult.getDescription().hasDescription()) {
		//     headerResult = new ParseResult(parseFromFilename(file), new ArrayList<>());
		// }

		return headerResult;
	}

	/**
	 * Parse file header for metadata
	 *
	 * @param file The input file
	 * @return ParseResult with metadata and validation issues
	 */
	private static ParseResult parseFileHeader(File file) {
		Map<String, String> metadata = new HashMap<>();
		List<String> dataLines = new ArrayList<>();

		System.out.println("[DEBUG] Starting to parse file: " + file.getName());

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			boolean inHeader = true;
			int dataLineCount = 0;

			while ((line = br.readLine()) != null) {
				String trimmed = line.trim();

				// Skip empty lines
				if (trimmed.isEmpty()) {
					continue;
				}

				// Skip separator lines (===, ---, etc.)
				if (trimmed.matches("^[=\\-]+.*[=\\-]+$")) {
					continue;
				}

				// Check if this line contains a colon (key: value format)
				int colonIdx = trimmed.indexOf(':');
				if (colonIdx > 0 && inHeader) {
					// Remove leading # if present
					String metaLine = trimmed.startsWith("#") ? trimmed.substring(1).trim() : trimmed;

					// Re-find colon after removing #
					colonIdx = metaLine.indexOf(':');
					if (colonIdx > 0) {
						String key = metaLine.substring(0, colonIdx).trim();
						String value = metaLine.substring(colonIdx + 1).trim();

						// Skip section header lines like "Experiment Description"
						if (value.isEmpty() || key.equalsIgnoreCase("BMDExpress Metadata") ||
						    key.equalsIgnoreCase("Experiment Description")) {
							continue;
						}

						metadata.put(key.toLowerCase(), value);
					}
				} else if (trimmed.contains("\t")) {
					// This looks like tab-delimited data (actual data, not metadata)
					dataLineCount++;
					if (dataLineCount >= 2) {
						// We've hit the actual data section
						inHeader = false;
						break;
					}
				}
			}
		} catch (IOException e) {
			// If we can't read the file, return empty result
			return new ParseResult(new InVivoExperimentDescription(), new ArrayList<>());
		}

		// Debug: Show what we found
		System.out.println("[DEBUG] Finished parsing. Found " + metadata.size() + " metadata entries");

		// If no metadata found, return empty
		if (metadata.isEmpty()) {
			System.out.println("[DEBUG] No metadata found, returning empty description");
			return new ParseResult(new InVivoExperimentDescription(), new ArrayList<>());
		}

		// Debug: Print what metadata was extracted
		System.out.println("[DEBUG] Parsed metadata from file header:");
		for (Map.Entry<String, String> entry : metadata.entrySet()) {
			System.out.println("  " + entry.getKey() + " = " + entry.getValue());
		}

		// Build experiment description from metadata and validate
		return buildFromMetadata(metadata);
	}

	/**
	 * Build experiment description from parsed metadata and validate
	 */
	private static ParseResult buildFromMetadata(Map<String, String> metadata) {
		List<ValidationIssue> issues = new ArrayList<>();

		// Determine if in vitro or in vivo
		boolean isInVitro = metadata.containsKey("cell line") ||
		                    metadata.containsKey("cellline") ||
		                    (metadata.containsKey("type") && metadata.get("type").toLowerCase().contains("vitro"));

		ExperimentDescriptionBase desc;

		if (isInVitro) {
			InVitroExperimentDescription inVitro = new InVitroExperimentDescription();

			// Parse cell line
			String cellLine = getMetadataValue(metadata, "cell line", "cellline");
			if (cellLine != null) {
				inVitro.setCellLine(cellLine);
			}

			desc = inVitro;
		} else {
			InVivoExperimentDescription inVivo = new InVivoExperimentDescription();

			// Parse and validate species
			String species = getMetadataValue(metadata, "species");
			if (species != null) {
				ValidationResult validation = validateSpecies(species);
				if (validation.isValid()) {
					inVivo.setSpecies(validation.getValue());
				} else {
					// Set anyway even if validation fails - user can correct in dialog
					inVivo.setSpecies(species);
					issues.add(new ValidationIssue("Species", species, validation.getSuggestion(),
						InVivoExperimentDescription.SPECIES_VOCABULARY));
				}
			}

			// Parse and validate strain
			String strain = getMetadataValue(metadata, "strain");
			if (strain != null) {
				inVivo.setStrain(normalizeStrain(strain));
			}

			// Parse and validate sex
			String sex = getMetadataValue(metadata, "sex");
			if (sex != null) {
				inVivo.setSex(capitalizeFist(sex));
			}

			// Parse and validate organ
			String organ = getMetadataValue(metadata, "organ");
			if (organ != null) {
				ValidationResult validation = validateOrgan(organ);
				if (validation.isValid()) {
					inVivo.setOrgan(validation.getValue());
				} else {
					// Set anyway even if validation fails - user can correct in dialog
					inVivo.setOrgan(organ);
					issues.add(new ValidationIssue("Organ", organ, validation.getSuggestion(),
						InVivoExperimentDescription.ORGAN_VOCABULARY));
				}
			}

			desc = inVivo;
		}

		// Parse test article (common to both)
		TestArticleIdentifier testArticle = parseTestArticle(metadata);
		if (testArticle != null) {
			desc.setTestArticle(testArticle);
		}

		// Parse route of administration
		RouteOfAdministrationBase route = parseRoute(metadata);
		if (route != null) {
			desc.setRouteOfAdministration(route);
		}

		// Parse study duration
		String duration = getMetadataValue(metadata, "study duration", "duration");
		if (duration != null) {
			desc.setStudyDuration(duration);
		}

		return new ParseResult(desc, issues);
	}

	/**
	 * Parse test article from metadata
	 */
	private static TestArticleIdentifier parseTestArticle(Map<String, String> metadata) {
		String name = getMetadataValue(metadata, "test article", "chemical", "compound");
		String casrn = getMetadataValue(metadata, "casrn", "cas", "cas number");
		String dsstox = getMetadataValue(metadata, "dsstox", "dsstox id");

		if (name != null || casrn != null || dsstox != null) {
			return new TestArticleIdentifier(name, casrn, dsstox);
		}
		return null;
	}

	/**
	 * Parse route of administration from metadata
	 */
	private static RouteOfAdministrationBase parseRoute(Map<String, String> metadata) {
		String route = getMetadataValue(metadata, "route", "route of administration");
		if (route == null) {
			return null;
		}

		String routeLower = route.toLowerCase();
		String vehicle = getMetadataValue(metadata, "vehicle");

		if (routeLower.contains("inhalation")) {
			InhalationRoute inhalation = new InhalationRoute();
			if (routeLower.contains("aerosol")) {
				inhalation.setInhalationType(InhalationRoute.InhalationType.AEROSOL);
			} else if (routeLower.contains("gas")) {
				inhalation.setInhalationType(InhalationRoute.InhalationType.GAS);
			}
			return inhalation;
		} else if (routeLower.contains("oral")) {
			OralRoute oral = new OralRoute();
			if (routeLower.contains("gavage")) {
				oral.setOralType(OralRoute.OralType.GAVAGE);
				if (vehicle != null) {
					oral.setVehicle(vehicle);
				}
			} else if (routeLower.contains("feed") || routeLower.contains("diet")) {
				oral.setOralType(OralRoute.OralType.FEED);
			} else if (routeLower.contains("water")) {
				oral.setOralType(OralRoute.OralType.WATER);
			}
			return oral;
		} else if (routeLower.contains("transdermal") || routeLower.contains("dermal")) {
			TransdermalRoute transdermal = new TransdermalRoute();
			if (vehicle != null) {
				transdermal.setVehicle(vehicle);
			}
			return transdermal;
		} else if (routeLower.contains("iv") || routeLower.contains("intravenous")) {
			IntravenousRoute iv = new IntravenousRoute();
			if (vehicle != null) {
				iv.setVehicle(vehicle);
			}
			return iv;
		}

		return null;
	}

	/**
	 * Get metadata value by trying multiple possible keys
	 */
	private static String getMetadataValue(Map<String, String> metadata, String... keys) {
		for (String key : keys) {
			String value = metadata.get(key.toLowerCase());
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Validate species against controlled vocabulary
	 */
	private static ValidationResult validateSpecies(String species) {
		// Exact match
		for (String valid : InVivoExperimentDescription.SPECIES_VOCABULARY) {
			if (valid.equalsIgnoreCase(species)) {
				return new ValidationResult(true, valid, null);
			}
		}

		// Fuzzy match - find closest
		String suggestion = findClosestMatch(species, InVivoExperimentDescription.SPECIES_VOCABULARY);
		return new ValidationResult(false, null, suggestion);
	}

	/**
	 * Validate organ against controlled vocabulary
	 */
	private static ValidationResult validateOrgan(String organ) {
		// Exact match
		for (String valid : InVivoExperimentDescription.ORGAN_VOCABULARY) {
			if (valid.equalsIgnoreCase(organ)) {
				return new ValidationResult(true, valid, null);
			}
		}

		// Fuzzy match - find closest
		String suggestion = findClosestMatch(organ, InVivoExperimentDescription.ORGAN_VOCABULARY);
		return new ValidationResult(false, null, suggestion);
	}

	/**
	 * Find closest match using simple Levenshtein distance
	 */
	private static String findClosestMatch(String input, List<String> vocabulary) {
		if (input == null || vocabulary == null || vocabulary.isEmpty()) {
			return null;
		}

		int minDistance = Integer.MAX_VALUE;
		String closest = null;

		for (String candidate : vocabulary) {
			int distance = levenshteinDistance(input.toLowerCase(), candidate.toLowerCase());
			if (distance < minDistance) {
				minDistance = distance;
				closest = candidate;
			}
		}

		// Only suggest if reasonably close (within 3 edits)
		return minDistance <= 3 ? closest : null;
	}

	/**
	 * Calculate Levenshtein distance between two strings
	 */
	private static int levenshteinDistance(String s1, String s2) {
		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			dp[i][0] = i;
		}
		for (int j = 0; j <= s2.length(); j++) {
			dp[0][j] = j;
		}

		for (int i = 1; i <= s1.length(); i++) {
			for (int j = 1; j <= s2.length(); j++) {
				int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
				dp[i][j] = Math.min(Math.min(
					dp[i - 1][j] + 1,      // deletion
					dp[i][j - 1] + 1),     // insertion
					dp[i - 1][j - 1] + cost  // substitution
				);
			}
		}

		return dp[s1.length()][s2.length()];
	}

	/**
	 * Parse filename to extract experimental metadata (fallback when no header)
	 *
	 * @param file The input file
	 * @return InVivoExperimentDescription with parsed fields, or empty description if parsing fails
	 */
	public static InVivoExperimentDescription parseFromFilename(File file) {
		if (file == null) {
			return new InVivoExperimentDescription();
		}

		String filename = file.getName();

		// Remove file extension
		if (filename.contains(".")) {
			filename = filename.substring(0, filename.lastIndexOf("."));
		}

		return parseFromString(filename);
	}

	/**
	 * Parse a string (filename without extension) to extract experimental metadata
	 *
	 * @param input The input string to parse
	 * @return InVivoExperimentDescription with parsed fields
	 */
	public static InVivoExperimentDescription parseFromString(String input) {
		if (input == null || input.isEmpty()) {
			return new InVivoExperimentDescription();
		}

		InVivoExperimentDescription desc = new InVivoExperimentDescription();

		// Normalize: replace underscores, hyphens, and periods with spaces, convert to lowercase
		String normalized = input.replaceAll("[_\\-.]", " ").toLowerCase();
		String[] parts = normalized.split("\\s+");

		// Extract known keywords
		for (String part : parts) {
			if (part.isEmpty()) continue;

			// Check for sex
			if (desc.getSex() == null && SEX_KEYWORDS.contains(part)) {
				desc.setSex(capitalizeFist(part));
			}

			// Check for organ
			if (desc.getOrgan() == null && ORGAN_KEYWORDS.contains(part)) {
				desc.setOrgan(capitalizeFist(part));
			}

			// Check for species
			if (desc.getSpecies() == null && SPECIES_KEYWORDS.contains(part)) {
				desc.setSpecies(capitalizeFist(part));
			}

			// Check for strain
			if (desc.getStrain() == null && STRAIN_KEYWORDS.contains(part)) {
				desc.setStrain(normalizeStrain(part));
			}
		}

		// Try to extract test article (the parts not identified as other metadata)
		String testArticleName = extractTestArticleName(parts, desc);
		if (testArticleName != null && !testArticleName.isEmpty()) {
			TestArticleIdentifier ta = new TestArticleIdentifier(testArticleName, null, null);
			desc.setTestArticle(ta);
		}

		return desc;
	}

	/**
	 * Extract test article name by removing known metadata terms
	 */
	private static String extractTestArticleName(String[] parts, InVivoExperimentDescription desc) {
		StringBuilder testArticle = new StringBuilder();

		for (String part : parts) {
			if (part.isEmpty()) continue;

			// Skip if it's a known metadata keyword
			if (SEX_KEYWORDS.contains(part) ||
			    ORGAN_KEYWORDS.contains(part) ||
			    SPECIES_KEYWORDS.contains(part) ||
			    STRAIN_KEYWORDS.contains(part)) {
				continue;
			}

			// Skip project codes (P1, P2, etc.) and expression/data indicators
			if (part.matches("p\\d+") || part.matches("expression\\d*") ||
			    part.equals("data") || part.equals("expr") || part.equals("nooutlier")) {
				continue;
			}

			// Add to test article
			if (testArticle.length() > 0) {
				testArticle.append(" ");
			}
			testArticle.append(capitalizeFist(part));
		}

		return testArticle.toString().trim();
	}

	/**
	 * Capitalize first letter of a word
	 */
	private static String capitalizeFist(String word) {
		if (word == null || word.isEmpty()) {
			return word;
		}
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	/**
	 * Normalize strain names appropriately
	 */
	private static String normalizeStrain(String strain) {
		switch (strain.toLowerCase()) {
			case "spraguedawley":
			case "sd":
			case "sprague-dawley":
			case "sprague dawley":
				return "Sprague-Dawley";
			case "fischer":
			case "fischer 344":
				return "Fischer 344";
			case "wistar":
				return "Wistar";
			case "c57bl6":
			case "c57bl/6":
				return "C57BL/6";
			case "balb":
			case "balb/c":
				return "BALB/c";
			case "cd1":
			case "cd-1":
				return "CD-1";
			default:
				return capitalizeFist(strain);
		}
	}

	/**
	 * Check if the description has any parsed content
	 */
	public static boolean hasAnyParsedData(ExperimentDescriptionBase desc) {
		return desc != null && desc.hasDescription();
	}

	/**
	 * Result of parsing with validation issues
	 */
	public static class ParseResult {
		private final ExperimentDescriptionBase description;
		private final List<ValidationIssue> issues;

		public ParseResult(ExperimentDescriptionBase description, List<ValidationIssue> issues) {
			this.description = description;
			this.issues = issues;
		}

		public ExperimentDescriptionBase getDescription() {
			return description;
		}

		public List<ValidationIssue> getIssues() {
			return issues;
		}

		public boolean hasIssues() {
			return issues != null && !issues.isEmpty();
		}
	}

	/**
	 * Validation issue for a metadata field
	 */
	public static class ValidationIssue {
		private final String fieldName;
		private final String providedValue;
		private final String suggestedValue;
		private final List<String> validOptions;

		public ValidationIssue(String fieldName, String providedValue, String suggestedValue, List<String> validOptions) {
			this.fieldName = fieldName;
			this.providedValue = providedValue;
			this.suggestedValue = suggestedValue;
			this.validOptions = validOptions;
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getProvidedValue() {
			return providedValue;
		}

		public String getSuggestedValue() {
			return suggestedValue;
		}

		public List<String> getValidOptions() {
			return validOptions;
		}
	}

	/**
	 * Result of validation
	 */
	private static class ValidationResult {
		private final boolean valid;
		private final String value;
		private final String suggestion;

		public ValidationResult(boolean valid, String value, String suggestion) {
			this.valid = valid;
			this.value = value;
			this.suggestion = suggestion;
		}

		public boolean isValid() {
			return valid;
		}

		public String getValue() {
			return value;
		}

		public String getSuggestion() {
			return suggestion;
		}
	}
}
