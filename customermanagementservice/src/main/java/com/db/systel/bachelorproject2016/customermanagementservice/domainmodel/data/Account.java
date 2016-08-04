package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Account {

	private String password;
	private String username;
	private Integer customerID;

	public Account(@JsonProperty("password") String password, @JsonProperty("username") String username,
			@JsonProperty("customerID") Integer customerID) {
		this.setPassword(password);
		this.setUsername(username);
		this.setCustomerID(customerID);
	}

	public String getPassword() {
		return password;
	}

	@JsonProperty("password")
	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	@JsonProperty("username")
	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getCustomerID() {
		return customerID;
	}

	@JsonProperty("customerID")
	public void setCustomerID(Integer customerID) {
		this.customerID = customerID;
	}

}
