package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * In vitro experiment description with cell line information.
 * Used for cell-based experiments rather than animal studies.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InVitroExperimentDescription extends ExperimentDescriptionBase {

	private static final long serialVersionUID = 1L;

	// In vitro specific field
	private String cellLine;  // Cell line used (e.g., "HepG2", "MCF-7", "HEK293")

	/**
	 * Default constructor
	 */
	public InVitroExperimentDescription() {
		super();
	}

	/**
	 * Constructor with all fields
	 */
	public InVitroExperimentDescription(TestArticleIdentifier testArticle,
	                                     RouteOfAdministrationBase routeOfAdministration,
	                                     String studyDuration,
	                                     String cellLine) {
		super(testArticle, routeOfAdministration, studyDuration);
		this.cellLine = cellLine;
	}

	// Getters and setters

	public String getCellLine() {
		return cellLine;
	}

	public void setCellLine(String cellLine) {
		this.cellLine = cellLine;
	}

	@Override
	public String getExperimentType() {
		return "In Vitro";
	}

	@Override
	public boolean hasDescription() {
		return hasBaseDescription() ||
		       (cellLine != null && !cellLine.isEmpty());
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Experiment Type: In Vitro\n");

		// Append base fields using helper method
		appendBaseFormattedString(sb);

		// Append InVitro-specific field
		if (cellLine != null && !cellLine.isEmpty()) {
			sb.append("Cell Line: ").append(cellLine).append("\n");
		}

		return sb.toString();
	}

	@Override
	public ExperimentDescriptionBase copy() {
		InVitroExperimentDescription copy = new InVitroExperimentDescription();

		// Copy base fields using helper method
		copyBaseFields(copy);

		// Copy InVitro-specific field
		copy.setCellLine(cellLine);

		return copy;
	}
}
