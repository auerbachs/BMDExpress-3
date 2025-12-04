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
		TestArticleIdentifier ta = getTestArticle();
		RouteOfAdministrationBase route = getRouteOfAdministration();
		String duration = getStudyDuration();

		return (ta != null && ta.hasIdentifier()) ||
		       (route != null) ||
		       (duration != null && !duration.isEmpty()) ||
		       (cellLine != null && !cellLine.isEmpty());
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Experiment Type: In Vitro\n");

		TestArticleIdentifier ta = getTestArticle();
		if (ta != null && ta.hasIdentifier()) {
			sb.append("Test Article: ").append(ta.getFormattedString()).append("\n");
		}

		RouteOfAdministrationBase route = getRouteOfAdministration();
		if (route != null) {
			sb.append("Route of Administration: ").append(route.getFormattedString()).append("\n");
		}

		String duration = getStudyDuration();
		if (duration != null && !duration.isEmpty()) {
			sb.append("Study Duration: ").append(duration).append("\n");
		}

		if (cellLine != null && !cellLine.isEmpty()) {
			sb.append("Cell Line: ").append(cellLine).append("\n");
		}

		return sb.toString();
	}

	@Override
	public ExperimentDescriptionBase copy() {
		InVitroExperimentDescription copy = new InVitroExperimentDescription();

		// Copy common fields
		if (getTestArticle() != null) {
			TestArticleIdentifier ta = getTestArticle();
			copy.setTestArticle(new TestArticleIdentifier(ta.getName(), ta.getCasrn(), ta.getDsstox()));
		}
		copy.setRouteOfAdministration(getRouteOfAdministration());  // Routes are immutable
		copy.setStudyDuration(getStudyDuration());

		// Copy in vitro specific field
		copy.setCellLine(cellLine);

		return copy;
	}
}
