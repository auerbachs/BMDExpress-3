package com.sciome.bmdexpress2.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sciome.bmdexpress2.mvp.model.info.ExperimentDescription;

/**
 * Utility class to parse experimental metadata from filenames.
 *
 * Example filename patterns:
 * - P2_Perfluoro_3_methoxypropanoic_acid_Male_Thyroid-expression1.txt
 * - PFAS_Female_Liver.csv
 * - Rat_SpragueDawley_Male_Kidney_ToxicantX.dat
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
	 * Parse filename to extract experimental metadata
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
				desc.setStrain(capitalizeStrain(part));
			}
		}

		// Try to extract test article (the parts not identified as other metadata)
		String testArticle = extractTestArticle(parts, desc);
		if (testArticle != null && !testArticle.isEmpty()) {
			desc.setTestArticle(testArticle);
		}

		return desc;
	}

	/**
	 * Extract test article by removing known metadata terms
	 */
	private static String extractTestArticle(String[] parts, ExperimentDescription desc) {
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
	 * Capitalize strain names appropriately
	 */
	private static String capitalizeStrain(String strain) {
		switch (strain.toLowerCase()) {
			case "spraguedawley":
			case "sd":
				return "Sprague-Dawley";
			case "fischer":
				return "Fischer 344";
			case "wistar":
				return "Wistar";
			case "c57bl6":
				return "C57BL/6";
			case "balb":
				return "BALB/c";
			case "cd1":
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
}
