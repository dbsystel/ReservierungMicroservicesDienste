package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Customer {

	private Integer customerID;
	private String firstName;
	private String lastName;
	private String address;
	private Integer houseNumber;
	private Integer postCode;
	private String city;
	private String eMailAddress;
	private String paymentMethod;

	public Customer(@JsonProperty("customerID") Integer customerID, @JsonProperty("firstName") String firstName,
			@JsonProperty("lastName") String lastName, @JsonProperty("address") String address,
			@JsonProperty("houseNumber") Integer houseNumber, @JsonProperty("postCode") Integer postCode,
			@JsonProperty("city") String city, @JsonProperty("eMailAddress") String eMailAddress, @JsonProperty("paymentMethod") String paymentMethod) {
		this.setCustomerID(customerID);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setAddress(address);
		this.setHouseNumber(houseNumber);
		this.setPostCode(postCode);
		this.setCity(city);
		this.seteMailAddress(eMailAddress);
		this.setPaymentMethod(paymentMethod);
	}

	public Customer(Integer customerID) {
		this.setCustomerID(customerID);
	}

	public Integer getCustomerID() {
		return customerID;
	}

	@JsonProperty("customerID")
	public void setCustomerID(Integer customerID) {
		this.customerID = customerID;
	}

	public String getFirstName() {
		return firstName;
	}

	@JsonProperty("firstName")
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@JsonProperty("lastName")
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	@JsonProperty("address")
	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getHouseNumber() {
		return houseNumber;
	}

	@JsonProperty("houseNumber")
	public void setHouseNumber(Integer houseNumber) {
		this.houseNumber = houseNumber;
	}

	public Integer getPostCode() {
		return postCode;
	}

	@JsonProperty("postCode")
	public void setPostCode(Integer postCode) {
		this.postCode = postCode;
	}

	public String getCity() {
		return city;
	}

	@JsonProperty("city")
	public void setCity(String city) {
		this.city = city;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	@JsonProperty("paymentMethod")
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String geteMailAddress() {
		return eMailAddress;
	}

	@JsonProperty("eMailAddress")
	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

}
