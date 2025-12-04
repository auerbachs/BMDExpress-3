package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Transdermal route of administration.
 * Test article delivered through the skin, optionally with a vehicle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransdermalRoute extends RouteOfAdministrationBase {

	private static final long serialVersionUID = 1L;

	private String vehicle;  // Vehicle used for transdermal delivery (e.g., "DMSO", "Ethanol")

	/**
	 * Default constructor
	 */
	public TransdermalRoute() {
	}

	/**
	 * Constructor with vehicle
	 */
	public TransdermalRoute(String vehicle) {
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
		return "Transdermal";
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder("Transdermal");
		if (vehicle != null && !vehicle.isEmpty()) {
			sb.append(" (Vehicle: ").append(vehicle).append(")");
		}
		return sb.toString();
	}
}
