package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SeatAllocation extends DatabaseObject {

	private Integer seatID;
	private Integer trainConnectionID;
	private Long departureTime;
	private Long arrivalTime;

	public SeatAllocation(@JsonProperty("seatID") Integer seatId, @JsonProperty("trainConnectionID") Integer trainConnectionID,
			@JsonProperty("departureTime") Long departureTime, @JsonProperty("arrivalTime") Long arrivalTime) {

		properties = new String[] { "seatID", "trainConnectionID", "departureTime",
				"arrivalTime" };
		
		columnAttributes = new String[] {"seat_id", "train_connection_id", "allocated_from", "allocated_until"};

		this.setSeatId(seatId);
		this.setTrainConnectionId(trainConnectionID);
		this.setDepartureTime(departureTime);
		this.setArrivalTime(arrivalTime);
	}

	public Integer getSeatId() {
		return seatID;
	}

	@JsonProperty("seatID")
	public void setSeatId(Integer seatId) {
		this.seatID = seatId;
	}

	public Integer getTrainConnectionId() {
		return trainConnectionID;
	}

	@JsonProperty("trainConnectionID")
	public void setTrainConnectionId(Integer trainConnectionID) {
		this.trainConnectionID = trainConnectionID;
	}


	public Long getDepartureTime() {
		return departureTime;
	}

	@JsonProperty("departureTime")
	public void setDepartureTime(Long departureTime) {
		this.departureTime = departureTime;
	}

	public Long getArrivalTime() {
		return arrivalTime;
	}

	@JsonProperty("arrivalTime")
	public void setArrivalTime(Long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

}
