package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Datenstruktur um Kunden und E-Mail-Adressen zuzuweisen 
 * 
 * Es können die E-Mail-Adressen für mehrere Kunden erfragt werden, daher ist die eindeutige Zuordnung notwendig
 */
public class CustomerContact {

	private String eMailAddress;
	private int customerID;

	public CustomerContact(@JsonProperty("customerID") int customerID,
			@JsonProperty("eMailAddress") String eMailAddress) {
		setCustomerID(customerID);
		setEMailAddress(eMailAddress);
	}

	public String getEMailAddress() {
		return this.eMailAddress;
	}

	@JsonProperty("eMailAddress")
	public void setEMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	public int getCustomerID() {
		return this.customerID;
	}

	@JsonProperty("customerID")
	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}
}
