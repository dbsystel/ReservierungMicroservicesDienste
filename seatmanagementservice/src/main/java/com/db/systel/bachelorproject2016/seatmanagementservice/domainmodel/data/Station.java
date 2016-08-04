package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Station extends DatabaseObject {

	private String name;

	public Station(@JsonProperty("name") String name) {
		properties = new String[] { "name" };
		columnAttributes = new String[] {"name"};
		setName(name);
	}

	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}
}
