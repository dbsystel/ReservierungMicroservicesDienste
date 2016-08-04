package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainConnection {

	private Integer id;
	private Integer trainNumber;
	private String trainCategory;
	/*
	 * Format: dd-MM-yyyy HH:mm
	 */
	private String departureTime;
	private String arrivalTime;
	private List<IntermediateStation> intermediateStations;

	public TrainConnection(@JsonProperty("id") Integer id, @JsonProperty("trainNumber") Integer trainNumber,
			@JsonProperty("trainCategory") String trainCategory, @JsonProperty("departureTime") String departureTime,
			@JsonProperty("arrivalTime") String arrivalTime,
			@JsonProperty("intermediateStations") List<IntermediateStation> intermediateStations) {
		this.setArrivalTime(arrivalTime);
		this.setDepartureTime(departureTime);
		this.setId(id);
		this.setIntermediateStations(intermediateStations);
		this.setTrainCategory(trainCategory);
		this.setTrainNumber(trainNumber);
	}

	public Integer getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTrainNumber() {
		return trainNumber;
	}

	@JsonProperty("trainNumber")
	public void setTrainNumber(Integer trainNumber) {
		this.trainNumber = trainNumber;
	}

	public String getTrainCategory() {
		return trainCategory;
	}

	@JsonProperty("trainCategory")
	public void setTrainCategory(String trainCategory) {
		this.trainCategory = trainCategory;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	@JsonProperty("departureTime")
	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	@JsonProperty("arrivalTime")
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public List<IntermediateStation> getIntermediateStations() {
		return intermediateStations;
	}

	@JsonProperty("intermediateStations")
	public void setIntermediateStations(List<IntermediateStation> intermediateStations) {
		this.intermediateStations = intermediateStations;
	}

}
