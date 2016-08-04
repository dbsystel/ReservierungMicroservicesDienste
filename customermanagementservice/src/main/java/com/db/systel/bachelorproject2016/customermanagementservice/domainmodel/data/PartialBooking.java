package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PartialBooking {

	private int partialBookingID;
	private int seatID;
	private int trainConnectionID;
	private String start;
	private String destination;
	private String day;
	private String price;

	public PartialBooking(@JsonProperty("partialBookingID") int partialBookingID, @JsonProperty("seatID") int seatID,
			@JsonProperty("trainConnectionID") int trainConnectionID, @JsonProperty("start") String start,
			@JsonProperty("destination") String destination, @JsonProperty("day") String day,
			@JsonProperty("price") String price) {
		setPartialBookingID(partialBookingID);
		setSeatID(seatID);
		setTrainConnectionID(trainConnectionID);
		setStart(start);
		setDestination(destination);
		setDay(day);
		setPrice(price);
	}

	public int getPartialBookingID() {
		return partialBookingID;
	}

	@JsonProperty("partialBookingID")
	public void setPartialBookingID(int partialBookingID) {
		this.partialBookingID = partialBookingID;
	}

	public int getSeatID() {
		return seatID;
	}

	@JsonProperty("seatID")
	public void setSeatID(int seatID) {
		this.seatID = seatID;
	}

	public int getTrainConnectionID() {
		return trainConnectionID;
	}

	@JsonProperty("trainConnectionID")
	public void setTrainConnectionID(int trainConnectionID) {
		this.trainConnectionID = trainConnectionID;
	}

	public String getStart() {
		return start;
	}

	@JsonProperty("start")
	public void setStart(String start) {
		this.start = start;
	}

	public String getDestination() {
		return destination;
	}

	@JsonProperty("destination")
	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDay() {
		return day;
	}

	@JsonProperty("day")
	public void setDay(String day) {
		this.day = day;
	}

	public String getPrice() {
		return price;
	}

	@JsonProperty("price")
	public void setPrice(String price) {
		this.price = price;
	}

}
