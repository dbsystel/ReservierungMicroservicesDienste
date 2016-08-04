package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Seat extends DatabaseObject {

	private Integer seatClass;
	private String seatArea;
	private String seatLocation;
	private String seatCompartmentType;
	private Boolean upperLevel;
	/*
	 * TODO: Brauchen wir das im Konstruktor?
	 */
	private String state;
	private Integer wagonID;

	public Seat(@JsonProperty("seatClass") Integer seatClass, @JsonProperty("seatArea") String seatArea,
			@JsonProperty("seatLocation") String seatLocation,
			@JsonProperty("seatCompartmentType") String seatCompartmentType,
			@JsonProperty("upperLevel") Boolean upperLevel, @JsonProperty("wagonID") Integer wagonID, 
			@JsonProperty("state") String state) {

		properties = new String[] { "seatClass", "seatArea", "seatLocation", "seatCompartmentType", "upperLevel",
				"wagonID", "state"};
		columnAttributes = new String[] {"class", "area", "location", "compartment_type", "upper_level", "wagon_id", "state"};
		this.setSeatClass(seatClass);
		this.setSeatArea(seatArea);
		this.setSeatLocation(seatLocation);
		this.setSeatSection(seatCompartmentType);
		this.setUpperLevel(upperLevel);
		this.setWagonID(wagonID);
		this.setState(state);
	}

	public Integer getSeatClass() {
		return seatClass;
	}

	@JsonProperty("seatClass")
	public void setSeatClass(Integer seatClass) {
		this.seatClass = seatClass;
	}

	public String getSeatArea() {
		return seatArea;
	}

	@JsonProperty("seatArea")
	public void setSeatArea(String seatArea) {
		this.seatArea = seatArea;
	}

	public String getSeatLocation() {
		return seatLocation;
	}

	@JsonProperty("seatLocation")
	public void setSeatLocation(String seatLocation) {
		this.seatLocation = seatLocation;
	}

	public String getSeatSection() {
		return seatCompartmentType;
	}

	@JsonProperty("seatCompartmentType")
	public void setSeatSection(String seatSection) {
		this.seatCompartmentType = seatSection;
	}

	public Boolean isUpperLevel() {
		return upperLevel;
	}

	@JsonProperty("upperLevel")
	public void setUpperLevel(Boolean upperLevel) {
		this.upperLevel = upperLevel;
	}

	public Integer getWagonID() {
		return wagonID;
	}
	
	@JsonProperty("wagonID")
	public void setWagonID(Integer wagonID) {
		this.wagonID = wagonID;
	}

	public String getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(String state) {
		this.state = state;
	}

}
