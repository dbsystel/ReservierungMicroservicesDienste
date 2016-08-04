package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainConnection extends DatabaseObject {

	private Integer routeID;
	private Integer trainID;
	private Long departureTime;
	private Integer dayOfWeek;

	public TrainConnection(@JsonProperty("routeID") Integer routeID, @JsonProperty("trainID") Integer train_id,
			@JsonProperty("departureTime") Long departureTime, @JsonProperty("dayOfWeek") Integer dayOfWeek) {

		properties = new String[] { "routeID", "trainID", "departureTime", "dayOfWeek" };
		columnAttributes = new String[] {"route_id", "train_id", "departure_time", "day_of_week"};
		setRouteId(routeID);
		setTrainId(train_id);
		setDepartureTime(departureTime);
		setDayOfWeek(dayOfWeek);
	}

	public Integer getRouteId() {
		return routeID;
	}
	
	@JsonProperty("routeID")
	public void setRouteId(Integer route_id) {
		this.routeID = route_id;
	}

	public Integer getTrainId() {
		return trainID;
	}

	@JsonProperty("trainID")
	public void setTrainId(Integer trainID) {
		this.trainID = trainID;
	}

	public Long getDepartureTime() {
		return departureTime;
	}

	@JsonProperty("departureTime")
	public void setDepartureTime(Long departureTime) {
		this.departureTime = departureTime;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	@JsonProperty("dayOfWeek")
	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

}
