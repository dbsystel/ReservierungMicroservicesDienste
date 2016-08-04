package com.db.systel.bachelorproject2016.bookingservice.domainmodel.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerBooking {

	private int CustomerID;
	private List<PartialBooking> partialBookings;

	public CustomerBooking(@JsonProperty("customerID") int customerID,
			@JsonProperty("partialBookings") List<PartialBooking> partialBookings) {
		setCustomerID(customerID);
		setPartialBookings(partialBookings);
	}

	public int getCustomerID() {
		return CustomerID;
	}

	@JsonProperty("customerID")
	public void setCustomerID(int customerID) {
		CustomerID = customerID;
	}

	public List<PartialBooking> getPartialBookings() {
		return partialBookings;
	}

	@JsonProperty("partialBookings")
	public void setPartialBookings(List<PartialBooking> partialBookings) {
		this.partialBookings = partialBookings;
	}

}