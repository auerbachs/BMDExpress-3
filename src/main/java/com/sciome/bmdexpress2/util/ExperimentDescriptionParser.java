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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(ExperimentDescriptionParser.class);

	// Metadata key constants for consistent lookups
	private static final String[] CELL_LINE_KEYS = {"cell line", "cellline"};
	private static final String[] SPECIES_KEYS = {"species"};
	private static final String[] STRAIN_KEYS = {"strain"};
	private static final String[] SEX_KEYS = {"sex"};
	private static final String[] ORGAN_KEYS = {"organ"};
	private static final String[] TEST_ARTICLE_KEYS = {"test article", "chemical", "compound"};
	private static final String[] CASRN_KEYS = {"casrn", "cas", "cas number"};
	private static final String[] DSSTOX_KEYS = {"dsstox", "dsstox id"};
	private static final String[] DURATION_KEYS = {"study duration", "duration"};
	private static final String[] SUBJECT_TYPE_KEYS = {"subject type", "subjecttype"};
	private static final String[] ARTICLE_ROUTE_KEYS = {"article route", "test article route"};
	private static final String[] ARTICLE_VEHICLE_KEYS = {"article vehicle", "test article vehicle", "vehicle"};
	private static final String[] ADMINISTRATION_MEANS_KEYS = {"administration means", "means of administration"};
	private static final String[] ARTICLE_TYPE_KEYS = {"article type", "test article type"};
	private static final String[] PLATFORM_KEYS = {"platform", "chip"};
	private static final String[] PROVIDER_KEYS = {"provider"};

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

		logger.debug("Starting to parse file: {}", file.getName());

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
		logger.debug("Finished parsing. Found {} metadata entries", metadata.size());

		// Debug: Print what metadata was extracted
		if (!metadata.isEmpty()) {
			logger.debug("Parsed metadata from file header:");
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				logger.debug("  {} = {}", entry.getKey(), entry.getValue());
			}
		} else {
			logger.debug("No metadata found in file header");
		}

		// Build experiment description from metadata and validate
		// If metadata is empty, validation will create issues for all missing required fields
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

			// Parse and validate cell line (REQUIRED)
			String cellLine = getMetadataValue(metadata, CELL_LINE_KEYS);
			if (cellLine == null || cellLine.trim().isEmpty()) {
				issues.add(new ValidationIssue("Cell Line", null, null, null));
			} else {
				inVitro.setCellLine(cellLine);
			}

			desc = inVitro;
		} else {
			InVivoExperimentDescription inVivo = new InVivoExperimentDescription();

			// Parse and validate InVivo-specific fields using helper method
			parseRequiredVocabularyField(metadata, issues, "Species",
				InVivoExperimentDescription.SPECIES_VOCABULARY,
				inVivo::setSpecies,
				SPECIES_KEYS);

			// Parse and validate strain (REQUIRED)
			// Note: Strain validation is context-dependent on species, so just normalize
			String strain = getMetadataValue(metadata, STRAIN_KEYS);
			if (strain == null || strain.trim().isEmpty()) {
				issues.add(new ValidationIssue("Strain", null, null, null));
			} else {
				inVivo.setStrain(normalizeStrain(strain));
			}

			parseRequiredVocabularyField(metadata, issues, "Sex",
				InVivoExperimentDescription.SEX_VOCABULARY,
				inVivo::setSex,
				SEX_KEYS);

			parseRequiredVocabularyField(metadata, issues, "Organ",
				InVivoExperimentDescription.ORGAN_VOCABULARY,
				inVivo::setOrgan,
				ORGAN_KEYS);

			desc = inVivo;
		}

		// Parse and validate test article (REQUIRED)
		String articleName = getMetadataValue(metadata, TEST_ARTICLE_KEYS);
		String casrn = getMetadataValue(metadata, CASRN_KEYS);
		String dsstox = getMetadataValue(metadata, DSSTOX_KEYS);

		// Validate CASRN (REQUIRED with format validation)
		if (casrn == null || casrn.trim().isEmpty()) {
			issues.add(new ValidationIssue("CASRN", null, "Expected format: NNNNNN-NN-N (e.g., '13252-13-6')", null));
		} else {
			ValidationResult validation = validateCASRN(casrn);
			if (!validation.isValid()) {
				issues.add(new ValidationIssue("CASRN", casrn, validation.getSuggestion(), null));
			} else if (validation.getValue() != null) {
				casrn = validation.getValue();  // Use validated/normalized value
			}
		}

		// Validate DSSTOX (REQUIRED with format validation)
		if (dsstox == null || dsstox.trim().isEmpty()) {
			issues.add(new ValidationIssue("DSSTOX", null, "Expected format: DTXSID followed by 7-9 digits (e.g., 'DTXSID3027446')", null));
		} else {
			ValidationResult validation = validateDSSTOX(dsstox);
			if (!validation.isValid()) {
				issues.add(new ValidationIssue("DSSTOX", dsstox, validation.getSuggestion(), null));
			} else if (validation.getValue() != null) {
				dsstox = validation.getValue();  // Use validated/normalized value
			}
		}

		// Create test article if at least one identifier is present
		if (articleName != null || casrn != null || dsstox != null) {
			TestArticleIdentifier testArticle = new TestArticleIdentifier(articleName, casrn, dsstox);
			if (testArticle.hasIdentifier()) {
				desc.setTestArticle(testArticle);
			} else {
				issues.add(new ValidationIssue("Test Article", null, null, null));
			}
		} else {
			issues.add(new ValidationIssue("Test Article", null, null, null));
		}

		// Parse route of administration (optional)
		RouteOfAdministrationBase route = parseRoute(metadata);
		if (route != null) {
			desc.setRouteOfAdministration(route);
		}

		// Parse and validate all required common fields using helper method
		parseRequiredVocabularyField(metadata, issues, "Study Duration",
			ExperimentDescriptionBase.STUDY_DURATION_VOCABULARY,
			desc::setStudyDuration,
			DURATION_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Subject Type",
			ExperimentDescriptionBase.SUBJECT_TYPE_VOCABULARY,
			desc::setSubjectType,
			SUBJECT_TYPE_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Article Route",
			ExperimentDescriptionBase.ARTICLE_ROUTE_VOCABULARY,
			desc::setArticleRoute,
			ARTICLE_ROUTE_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Article Vehicle",
			ExperimentDescriptionBase.ARTICLE_VEHICLE_VOCABULARY,
			desc::setArticleVehicle,
			ARTICLE_VEHICLE_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Administration Means",
			ExperimentDescriptionBase.ADMINISTRATION_MEANS_VOCABULARY,
			desc::setAdministrationMeans,
			ADMINISTRATION_MEANS_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Article Type",
			ExperimentDescriptionBase.ARTICLE_TYPE_VOCABULARY,
			desc::setArticleType,
			ARTICLE_TYPE_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Platform",
			ExperimentDescriptionBase.PLATFORM_VOCABULARY,
			desc::setPlatform,
			PLATFORM_KEYS);

		parseRequiredVocabularyField(metadata, issues, "Provider",
			ExperimentDescriptionBase.PROVIDER_VOCABULARY,
			desc::setProvider,
			PROVIDER_KEYS);

		return new ParseResult(desc, issues);
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
	 * Helper method to parse and validate a required field with controlled vocabulary.
	 * Eliminates code duplication for the common pattern of field validation.
	 *
	 * @param metadata The metadata map to read from
	 * @param issues The list to add validation issues to
	 * @param displayName Display name for error messages
	 * @param vocabulary The controlled vocabulary
	 * @param setter Lambda to set the validated value on the description object
	 * @param metadataKeys Possible metadata keys to look for
	 */
	private static void parseRequiredVocabularyField(
			Map<String, String> metadata,
			List<ValidationIssue> issues,
			String displayName,
			List<String> vocabulary,
			java.util.function.Consumer<String> setter,
			String... metadataKeys) {

		String value = getMetadataValue(metadata, metadataKeys);
		if (value == null || value.trim().isEmpty()) {
			issues.add(new ValidationIssue(displayName, null, null, vocabulary));
		} else {
			ValidationResult validation = validateAgainstVocabulary(value, vocabulary);
			if (validation.isValid()) {
				setter.accept(validation.getValue());
			} else {
				setter.accept(value);
				issues.add(new ValidationIssue(displayName, value, validation.getSuggestion(), vocabulary));
			}
		}
	}

	/**
	 * Generic validation method for any field with a controlled vocabulary.
	 * Performs exact match (case-insensitive) and provides fuzzy match suggestions if no exact match found.
	 *
	 * @param value The value to validate
	 * @param vocabulary The controlled vocabulary to validate against
	 * @return ValidationResult with valid flag, normalized value, and suggestion if invalid
	 */
	private static ValidationResult validateAgainstVocabulary(String value, List<String> vocabulary) {
		if (value == null || vocabulary == null) {
			return new ValidationResult(false, null, null);
		}

		// Exact match (case-insensitive)
		for (String valid : vocabulary) {
			if (valid.equalsIgnoreCase(value)) {
				return new ValidationResult(true, valid, null);
			}
		}

		// No exact match - find closest match for suggestion
		String suggestion = findClosestMatch(value, vocabulary);
		return new ValidationResult(false, null, suggestion);
	}

	/**
	 * Validate CASRN format
	 * Expected format: NNNNNN-NN-N (e.g., "13252-13-6", "50-00-0")
	 * Can have 2-10 digits before first hyphen, 2 digits after, and 1 check digit
	 */
	private static ValidationResult validateCASRN(String casrn) {
		if (casrn == null || casrn.trim().isEmpty()) {
			return new ValidationResult(true, null, null);
		}

		// Basic CASRN format: digits-digits-digit
		// Pattern allows flexibility in first segment length (2-10 digits)
		String casrnPattern = "^\\d{2,10}-\\d{2}-\\d$";

		if (casrn.matches(casrnPattern)) {
			return new ValidationResult(true, casrn, null);
		} else {
			return new ValidationResult(false, null,
				"Expected format: NNNNNN-NN-N (e.g., '13252-13-6')");
		}
	}

	/**
	 * Validate DSSTOX format
	 * Expected format: DTXSID followed by 7-9 digits (e.g., "DTXSID3027446", "DTXSID0020573")
	 */
	private static ValidationResult validateDSSTOX(String dsstox) {
		if (dsstox == null || dsstox.trim().isEmpty()) {
			return new ValidationResult(true, null, null);
		}

		// DSSTOX format: DTXSID followed by 7-9 digits
		String dsstoxPattern = "^DTXSID\\d{7,9}$";

		if (dsstox.toUpperCase().matches(dsstoxPattern)) {
			return new ValidationResult(true, dsstox.toUpperCase(), null);
		} else {
			return new ValidationResult(false, null,
				"Expected format: DTXSID followed by 7-9 digits (e.g., 'DTXSID3027446')");
		}
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
				desc.setSex(capitalizeFirst(part));
			}

			// Check for organ
			if (desc.getOrgan() == null && ORGAN_KEYWORDS.contains(part)) {
				desc.setOrgan(capitalizeFirst(part));
			}

			// Check for species
			if (desc.getSpecies() == null && SPECIES_KEYWORDS.contains(part)) {
				desc.setSpecies(capitalizeFirst(part));
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
			testArticle.append(capitalizeFirst(part));
		}

		return testArticle.toString().trim();
	}

	/**
	 * Capitalize first letter of a word
	 */
	private static String capitalizeFirst(String word) {
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
				return capitalizeFirst(strain);
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
