package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntermediateStation {

	private String name;
	/*
	 * Format: dd-MM-yyyy HH:mm
	 */
	private String timeOfStop;

	public IntermediateStation(@JsonProperty("name") String name, @JsonProperty("timeOfStop") String timeOfStop) {
		this.setName(name);
		this.setTimeOfStop(timeOfStop);
	}

	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	public String getTimeOfStop() {
		return timeOfStop;
	}

	@JsonProperty("timeOfStop")
	public void setTimeOfStop(String timeOfStop) {
		this.timeOfStop = timeOfStop;
	}

}
