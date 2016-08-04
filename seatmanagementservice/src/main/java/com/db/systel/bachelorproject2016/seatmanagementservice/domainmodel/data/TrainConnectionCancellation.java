package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainConnectionCancellation extends DatabaseObject {

	private Integer trainConnectionID;
	private Long day;

	public TrainConnectionCancellation(@JsonProperty("trainConnectionID") Integer trainConnectionID,
			@JsonProperty("day") Long day) {
		properties = new String[] { "trainConnectionID", "day" };
		columnAttributes = new String[] { "train_connection_id", "date" };
		this.setDate(day);
		this.setTrainConnectionID(trainConnectionID);
	}

	public Integer getTrainConnectionID() {
		return trainConnectionID;
	}

	@JsonProperty("trainConnectionID")
	public void setTrainConnectionID(Integer trainConnectionID) {
		this.trainConnectionID = trainConnectionID;
	}

	public Long getDate() {
		return day;
	}

	@JsonProperty("day")
	public void setDate(Long date) {
		this.day = date;
	}

}
