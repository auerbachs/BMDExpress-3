package com.sciome.bmdexpress2.mvp.model.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Oral route of administration.
 * Supports gavage (with vehicle), feed, or water delivery methods.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OralRoute extends RouteOfAdministrationBase {

	private static final long serialVersionUID = 1L;

	public enum OralType {
		GAVAGE,
		FEED,
		WATER
	}

	private OralType oralType;
	private String vehicle;  // Vehicle used for gavage delivery (optional)

	/**
	 * Default constructor
	 */
	public OralRoute() {
	}

	/**
	 * Constructor with oral type
	 */
	public OralRoute(OralType oralType) {
		this.oralType = oralType;
	}

	/**
	 * Constructor with oral type and vehicle
	 */
	public OralRoute(OralType oralType, String vehicle) {
		this.oralType = oralType;
		this.vehicle = vehicle;
	}

	public OralType getOralType() {
		return oralType;
	}

	public void setOralType(OralType oralType) {
		this.oralType = oralType;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	@Override
	public String getRouteType() {
		return "Oral";
	}

	@Override
	public String getFormattedString() {
		StringBuilder sb = new StringBuilder("Oral");
		if (oralType != null) {
			sb.append(" (").append(oralType.name());
			if (oralType == OralType.GAVAGE && vehicle != null && !vehicle.isEmpty()) {
				sb.append(", Vehicle: ").append(vehicle);
			}
			sb.append(")");
		}
		return sb.toString();
	}
}
