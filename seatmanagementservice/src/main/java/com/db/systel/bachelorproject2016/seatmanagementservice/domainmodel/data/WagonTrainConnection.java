package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WagonTrainConnection extends DatabaseObject {
	private Integer wagonID;
	private Integer trainConnectionID;
	private Integer number;

	public WagonTrainConnection(@JsonProperty("wagonID") Integer wagonID,
			@JsonProperty("trainConnectionID") Integer trainConnectionID, @JsonProperty("number") Integer number) {

		properties = new String[] { "wagonID", "trainConnectionID", "number" };
		columnAttributes = new String[] {"wagon_id", "train_connection_id", "wagon_number"};
		
		setWagonID(wagonID);
		setTrainConnectionID(trainConnectionID);
		setNumber(number);
	}

	public Integer getWagonID() {
		return wagonID;
	}

	@JsonProperty("wagonID")
	public void setWagonID(Integer wagonID) {
		this.wagonID = wagonID;
	}

	public Integer getTrainConnectionID() {
		return trainConnectionID;
	}

	@JsonProperty("trainConnectionID")
	public void setTrainConnectionID(Integer trainConnectionID) {
		this.trainConnectionID = trainConnectionID;
	}

	public Integer getNumber() {
		return number;
	}
	
	@JsonProperty("number")
	public void setNumber(Integer number) {
		this.number = number;
	}

}
