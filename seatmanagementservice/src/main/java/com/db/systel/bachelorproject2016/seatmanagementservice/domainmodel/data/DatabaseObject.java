package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DatabaseObject {
	/*
	 * Jedes Datenbankobjekt teilt mit, welche Properties es hat, damit wir
	 * drüber iterieren können
	 */
	@JsonIgnore
	public String[] properties;
	@JsonIgnore
	public String[] columnAttributes;

}
