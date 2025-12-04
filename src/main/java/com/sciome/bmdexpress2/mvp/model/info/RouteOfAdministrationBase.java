package com.sciome.bmdexpress2.mvp.model.info;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract base class for route of administration.
 * Defines how the test article is delivered to the subject.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = InhalationRoute.class, name = "inhalation"),
	@JsonSubTypes.Type(value = OralRoute.class, name = "oral"),
	@JsonSubTypes.Type(value = TransdermalRoute.class, name = "transdermal"),
	@JsonSubTypes.Type(value = IntravenousRoute.class, name = "intravenous")
})
public abstract class RouteOfAdministrationBase implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Get the route type name
	 */
	public abstract String getRouteType();

	/**
	 * Get formatted string representation
	 */
	public abstract String getFormattedString();

	@Override
	public String toString() {
		return getFormattedString();
	}
}
