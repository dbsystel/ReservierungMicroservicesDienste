package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Wagon extends DatabaseObject {

	private String state;

	public Wagon(@JsonProperty("state") String state) {
		properties = new String[] { "state" };
		columnAttributes = new String[] {"state"};
		setState(state);
	}

	public String getState() {
		return state;
	}

	@JsonProperty("state")
	public void setState(String state) {
		this.state = state;
	}

}
