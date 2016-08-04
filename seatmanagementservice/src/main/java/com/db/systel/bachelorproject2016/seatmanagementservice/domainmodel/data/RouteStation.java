package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteStation extends DatabaseObject {

	private Integer routeID;
	private Integer stationID;
	private Long stopTime;
	private Integer platform;

	public RouteStation(@JsonProperty("stationID") Integer stationId,
			@JsonProperty("routeID") Integer routeId, @JsonProperty("stopTime") Long stopTime,
			@JsonProperty("platform") Integer platform) {

		/*
		 * Properties zum iterieren festlegen
		 */
		properties = new String[] { "routeID", "stationID", "stopTime", "platform" };
		columnAttributes = new String[] {"route_id", "station_id", "time_of_stop", "platform"};
		this.setStationId(stationId);
		this.setRouteId(routeId);
		this.setStopTime(stopTime);
		this.setPlatform(platform);
	}

	public Integer getRouteId() {
		return routeID;
	}

	@JsonProperty("routeID")
	public void setRouteId(Integer routeId) {
		this.routeID = routeId;
	}

	public Integer getStationId() {
		return stationID;
	}

	@JsonProperty("stationID")
	public void setStationId(Integer stationId) {
		this.stationID = stationId;
	}

	public Long getStopTime() {
		return stopTime;
	}

	@JsonProperty("stopTime")
	public void setStopTime(Long stopTime) {
		this.stopTime = stopTime;
	}

	public Integer getPlatform() {
		return platform;
	}
	
	@JsonProperty("platform")
	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

}
