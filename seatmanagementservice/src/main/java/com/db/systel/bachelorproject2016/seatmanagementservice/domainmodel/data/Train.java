package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Train extends DatabaseObject {

	private Integer trainNumber;
	private String trainCategory;

	public Train(@JsonProperty("trainNumber") Integer trainNumber, @JsonProperty("trainCategory") String trainCategory) {

		properties = new String[] { "trainNumber", "trainCategory" };
		columnAttributes = new String[] {"number", "category"};
		setTrainNumber(trainNumber);
		setTrainCategory(trainCategory);
	}

	public Integer getTrainNumber() {
		return trainNumber;
	}

	@JsonProperty("trainNumber")
	public void setTrainNumber(Integer train_number) {
		this.trainNumber = train_number;
	}

	public String getTrainCategory() {
		return trainCategory;
	}

	@JsonProperty("trainCategory")
	public void setTrainCategory(String train_category) {
		this.trainCategory = train_category;
	}

}
