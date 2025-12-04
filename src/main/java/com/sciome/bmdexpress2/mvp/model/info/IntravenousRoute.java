package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Intravenous (IV) route of administration.
 * Test article delivered directly into the bloodstream, optionally with a vehicle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntravenousRoute extends RouteOfAdministrationBase {

	private static final long serialVersionUID = 1L;

	private String vehicle;  // Vehicle used for IV delivery (e.g., "Saline", "PBS")

	/**
	 * Default constructor
	 */
	public IntravenousRoute() {
	}

	/**
	 * Constructor with vehicle
	 */
	public IntravenousRoute(String vehicle) {
		this.vehicle = vehicle;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public String getRouteType() {
		return "Intravenous";
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder("Intravenous");
		if (vehicle != null && !vehicle.isEmpty()) {
			sb.append(" (Vehicle: ").append(vehicle).append(")");
		}
		return sb.toString();
	}
}
