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
		String platform = getPlatform();
		String provider = getProvider();
		String subjectType = getSubjectType();
		String articleRoute = getArticleRoute();
		String articleVehicle = getArticleVehicle();
		String administrationMeans = getAdministrationMeans();
		String articleType = getArticleType();

		return (ta != null && ta.hasIdentifier()) ||
		       (route != null) ||
		       (duration != null && !duration.isEmpty()) ||
		       (platform != null && !platform.isEmpty()) ||
		       (provider != null && !provider.isEmpty()) ||
		       (subjectType != null && !subjectType.isEmpty()) ||
		       (articleRoute != null && !articleRoute.isEmpty()) ||
		       (articleVehicle != null && !articleVehicle.isEmpty()) ||
		       (administrationMeans != null && !administrationMeans.isEmpty()) ||
		       (articleType != null && !articleType.isEmpty()) ||
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

		String subjectType = getSubjectType();
		if (subjectType != null && !subjectType.isEmpty()) {
			sb.append("Subject Type: ").append(subjectType).append("\n");
		}

		String articleRoute = getArticleRoute();
		if (articleRoute != null && !articleRoute.isEmpty()) {
			sb.append("Article Route: ").append(articleRoute).append("\n");
		}

		String articleVehicle = getArticleVehicle();
		if (articleVehicle != null && !articleVehicle.isEmpty()) {
			sb.append("Article Vehicle: ").append(articleVehicle).append("\n");
		}

		String administrationMeans = getAdministrationMeans();
		if (administrationMeans != null && !administrationMeans.isEmpty()) {
			sb.append("Administration Means: ").append(administrationMeans).append("\n");
		}

		RouteOfAdministrationBase route = getRouteOfAdministration();
		if (route != null) {
			sb.append("Route of Administration: ").append(route.getFormattedString()).append("\n");
		}

		String duration = getStudyDuration();
		if (duration != null && !duration.isEmpty()) {
			sb.append("Study Duration: ").append(duration).append("\n");
		}

		String platform = getPlatform();
		if (platform != null && !platform.isEmpty()) {
			sb.append("Platform: ").append(platform).append("\n");
		}

		String provider = getProvider();
		if (provider != null && !provider.isEmpty()) {
			sb.append("Provider: ").append(provider).append("\n");
		}

		String articleType = getArticleType();
		if (articleType != null && !articleType.isEmpty()) {
			sb.append("Article Type: ").append(articleType).append("\n");
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
		copy.setPlatform(getPlatform());
		copy.setProvider(getProvider());
		copy.setSubjectType(getSubjectType());
		copy.setArticleRoute(getArticleRoute());
		copy.setArticleVehicle(getArticleVehicle());
		copy.setAdministrationMeans(getAdministrationMeans());
		copy.setArticleType(getArticleType());

		// Copy in vitro specific field
		copy.setCellLine(cellLine);

		return copy;
	}
}
