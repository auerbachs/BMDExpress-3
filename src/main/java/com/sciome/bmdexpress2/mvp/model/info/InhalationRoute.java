package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inhalation route of administration.
 * Specifies whether the test article was delivered as gas or aerosol.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InhalationRoute extends RouteOfAdministrationBase {

	private static final long serialVersionUID = 1L;

	public enum InhalationType {
		GAS,
		AEROSOL
	}

	private InhalationType inhalationType;

	/**
	 * Default constructor
	 */
	public InhalationRoute() {
	}

	/**
	 * Constructor with inhalation type
	 */
	public InhalationRoute(InhalationType inhalationType) {
		this.inhalationType = inhalationType;
	}

	public InhalationType getInhalationType() {
		return inhalationType;
	}

	public void setInhalationType(InhalationType inhalationType) {
		this.inhalationType = inhalationType;
	}

	@Override
	public String getRouteType() {
		return "Inhalation";
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder("Inhalation");
		if (inhalationType != null) {
			sb.append(" (").append(inhalationType.name()).append(")");
		}
		return sb.toString();
	}
}
