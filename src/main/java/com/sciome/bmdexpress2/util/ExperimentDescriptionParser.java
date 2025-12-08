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

import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescription;
import com.sciome.bmdexpress2.mvp.model.info.TestArticleIdentifier;
import com.sciome.bmdexpress2.mvp.model.info.rules.MetadataFacts;
import com.sciome.bmdexpress2.mvp.model.info.rules.MetadataValidator;

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
			return new ParseResult(new ExperimentDescription(), new ArrayList<>());
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
			return new ParseResult(new ExperimentDescription(), new ArrayList<>());
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
	 * Build experiment description from parsed metadata and validate using Easy Rules.
	 *
	 * This method:
	 * 1. Parses all metadata fields into the description object
	 * 2. Validates format and vocabulary (for suggestions)
	 * 3. Runs Easy Rules validation for dependency and applicability rules
	 * 4. Combines all validation issues
	 */
	private static ParseResult buildFromMetadata(Map<String, String> metadata) {
		List<ValidationIssue> issues = new ArrayList<>();

		// Determine subject type from metadata
		String subjectType = getMetadataValue(metadata, SUBJECT_TYPE_KEYS);
		boolean isInVitro = "in vitro".equalsIgnoreCase(subjectType) ||
		                    metadata.containsKey("cell line") ||
		                    metadata.containsKey("cellline");

		// Create unified experiment description
		ExperimentDescription desc = new ExperimentDescription();
		String cellLine = null;
		String species = null;
		String strain = null;
		String sex = null;
		String organ = null;

		if (isInVitro) {
			// Set subject type for in vitro
			desc.setSubjectType("in vitro");

			// Parse cell line
			cellLine = getMetadataValue(metadata, CELL_LINE_KEYS);
			if (cellLine != null && !cellLine.isEmpty()) {
				desc.setCellLine(cellLine);
			}
		} else {
			// Set subject type for in vivo (default)
			if (subjectType == null || subjectType.isEmpty()) {
				desc.setSubjectType("in vivo");
			}

			// Parse species with vocabulary validation
			species = parseVocabularyField(metadata, issues, "Species",
				ExperimentDescription.SPECIES_VOCABULARY,
				desc::setSpecies,
				SPECIES_KEYS);

			// Parse strain (normalize but don't require - Easy Rules will check)
			strain = getMetadataValue(metadata, STRAIN_KEYS);
			if (strain != null && !strain.isEmpty()) {
				strain = normalizeStrain(strain);
				desc.setStrain(strain);
			}

			// Parse sex with vocabulary validation
			sex = parseVocabularyField(metadata, issues, "Sex",
				ExperimentDescription.SEX_VOCABULARY,
				desc::setSex,
				SEX_KEYS);

			// Parse organ with vocabulary validation
			organ = parseVocabularyField(metadata, issues, "Organ",
				ExperimentDescription.ORGAN_VOCABULARY,
				desc::setOrgan,
				ORGAN_KEYS);
		}

		// Parse test article identifiers
		String articleName = getMetadataValue(metadata, TEST_ARTICLE_KEYS);
		String casrn = getMetadataValue(metadata, CASRN_KEYS);
		String dsstox = getMetadataValue(metadata, DSSTOX_KEYS);

		// Validate CASRN format if provided
		if (casrn != null && !casrn.isEmpty()) {
			ValidationResult validation = validateCASRN(casrn);
			if (!validation.isValid()) {
				issues.add(new ValidationIssue("CASRN", casrn, validation.getSuggestion(), null));
			} else if (validation.getValue() != null) {
				casrn = validation.getValue();
			}
		}

		// Validate DSSTOX format if provided
		if (dsstox != null && !dsstox.isEmpty()) {
			ValidationResult validation = validateDSSTOX(dsstox);
			if (!validation.isValid()) {
				issues.add(new ValidationIssue("DSSTOX", dsstox, validation.getSuggestion(), null));
			} else if (validation.getValue() != null) {
				dsstox = validation.getValue();
			}
		}

		// Create test article if any identifier is present
		if (articleName != null || casrn != null || dsstox != null) {
			TestArticleIdentifier testArticle = new TestArticleIdentifier(articleName, casrn, dsstox);
			desc.setTestArticle(testArticle);
		}

		// Parse common fields with vocabulary validation
		String studyDuration = parseVocabularyField(metadata, issues, "Study Duration",
			ExperimentDescription.STUDY_DURATION_VOCABULARY,
			desc::setStudyDuration,
			DURATION_KEYS);

		// Set subject type if provided in metadata (override the default set above)
		if (subjectType != null && !subjectType.isEmpty()) {
			ValidationResult validation = validateAgainstVocabulary(subjectType, ExperimentDescription.SUBJECT_TYPE_VOCABULARY);
			if (validation.isValid()) {
				subjectType = validation.getValue();
				desc.setSubjectType(subjectType);
			} else {
				desc.setSubjectType(subjectType);
				if (validation.getSuggestion() != null) {
					issues.add(new ValidationIssue("Subject Type", subjectType, validation.getSuggestion(),
						ExperimentDescription.SUBJECT_TYPE_VOCABULARY));
				}
			}
		}

		// Parse article route, type, vehicle, and administration means
		String articleRoute = parseVocabularyField(metadata, issues, "Article Route",
			ExperimentDescription.ARTICLE_ROUTE_VOCABULARY,
			desc::setArticleRoute,
			ARTICLE_ROUTE_KEYS);

		String articleType = parseVocabularyField(metadata, issues, "Article Type",
			ExperimentDescription.ARTICLE_TYPE_VOCABULARY,
			desc::setArticleType,
			ARTICLE_TYPE_KEYS);

		String articleVehicle = parseVocabularyField(metadata, issues, "Article Vehicle",
			ExperimentDescription.ARTICLE_VEHICLE_VOCABULARY,
			desc::setArticleVehicle,
			ARTICLE_VEHICLE_KEYS);

		String administrationMeans = parseVocabularyField(metadata, issues, "Administration Means",
			ExperimentDescription.ADMINISTRATION_MEANS_VOCABULARY,
			desc::setAdministrationMeans,
			ADMINISTRATION_MEANS_KEYS);

		String platform = parseVocabularyField(metadata, issues, "Platform",
			ExperimentDescription.PLATFORM_VOCABULARY,
			desc::setPlatform,
			PLATFORM_KEYS);

		String provider = parseVocabularyField(metadata, issues, "Provider",
			ExperimentDescription.PROVIDER_VOCABULARY,
			desc::setProvider,
			PROVIDER_KEYS);

		// === Run Easy Rules validation for dependencies and applicability ===
		MetadataFacts facts = new MetadataFacts()
			.subjectType(subjectType)
			.species(species)
			.strain(strain)
			.sex(sex)
			.organ(organ)
			.cellLine(cellLine)
			.testArticle(articleName)
			.casrn(casrn)
			.dsstox(dsstox)
			.studyDuration(studyDuration)
			.articleType(articleType)
			.articleRoute(articleRoute)
			.articleVehicle(articleVehicle)
			.administrationMeans(administrationMeans)
			.platform(platform)
			.provider(provider);

		// Run the rules engine
		MetadataValidator.validateMetadata(facts);

		// Convert Easy Rules errors to ValidationIssue objects
		// Put the error message in providedValue so it's displayed prominently
		for (String error : facts.getErrors()) {
			issues.add(new ValidationIssue("Incompatibility", error, null, null));
		}

		// Add warnings as lower-priority issues (they don't block import)
		for (String warning : facts.getWarnings()) {
			logger.warn("Metadata warning: {}", warning);
		}

		return new ParseResult(desc, issues);
	}

	/**
	 * Parse a field with vocabulary validation, returning the parsed value.
	 * Does NOT add missing field errors - that's handled by Easy Rules.
	 */
	private static String parseVocabularyField(
			Map<String, String> metadata,
			List<ValidationIssue> issues,
			String displayName,
			List<String> vocabulary,
			java.util.function.Consumer<String> setter,
			String... metadataKeys) {

		String value = getMetadataValue(metadata, metadataKeys);
		if (value == null || value.trim().isEmpty()) {
			return null;  // Let Easy Rules handle missing required fields
		}

		ValidationResult validation = validateAgainstVocabulary(value, vocabulary);
		if (validation.isValid()) {
			setter.accept(validation.getValue());
			return validation.getValue();
		} else {
			setter.accept(value);
			// Only add vocabulary mismatch issue if we have a suggestion
			if (validation.getSuggestion() != null) {
				issues.add(new ValidationIssue(displayName, value, validation.getSuggestion(), vocabulary));
			}
			return value;
		}
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
	 * @return ExperimentDescription with parsed fields, or empty description if parsing fails
	 */
	public static ExperimentDescription parseFromFilename(File file) {
		if (file == null) {
			return new ExperimentDescription();
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
	 * @return ExperimentDescription with parsed fields
	 */
	public static ExperimentDescription parseFromString(String input) {
		if (input == null || input.isEmpty()) {
			return new ExperimentDescription();
		}

		ExperimentDescription desc = new ExperimentDescription();
		desc.setSubjectType("in vivo"); // Default to in vivo for filename parsing

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
	private static String extractTestArticleName(String[] parts, ExperimentDescription desc) {
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
	public static boolean hasAnyParsedData(ExperimentDescription desc) {
		return desc != null && desc.hasDescription();
	}

	/**
	 * Result of parsing with validation issues
	 */
	public static class ParseResult {
		private final ExperimentDescription description;
		private final List<ValidationIssue> issues;

		public ParseResult(ExperimentDescription description, List<ValidationIssue> issues) {
			this.description = description;
			this.issues = issues;
		}

		public ExperimentDescription getDescription() {
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
