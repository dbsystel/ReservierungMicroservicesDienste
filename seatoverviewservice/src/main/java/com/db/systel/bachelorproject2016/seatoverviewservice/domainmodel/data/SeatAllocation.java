package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SeatAllocation {

    private static final Logger logger = LoggerFactory.getLogger(SeatAllocation.class);

    private Integer seatID;

    private Integer trainConnectionID;

    private Long departureTime;

    private Long arrivalTime;

    public SeatAllocation(@JsonProperty("seatID") Integer seatID,
            @JsonProperty("trainConnectionID") Integer trainConnectionID,
            @JsonProperty("departureTime") String departureTime, @JsonProperty("arrivalTime") String arrivalTime) {

        this.setSeatID(seatID);
        this.setArrivalTime(arrivalTime);
        this.setDepartureTime(departureTime);
        this.setTrainConnectionID(trainConnectionID);
    }

    public SeatAllocation(Integer seatID, Integer trainConnectionID, Long departureTime, Long arrivalTime) {

        this.setSeatID(seatID);
        this.setArrivalTime(arrivalTime);
        this.setDepartureTime(departureTime);
        this.setTrainConnectionID(trainConnectionID);
    }

    public Integer getSeatID() {
        return seatID;
    }

    @JsonProperty("seatID")
    public void setSeatID(Integer seatID) {
        this.seatID = seatID;
    }

    public Integer getTrainConnectionID() {
        return trainConnectionID;
    }

    @JsonProperty("trainConnectionID")
    public void setTrainConnectionID(Integer trainConnectionID) {
        this.trainConnectionID = trainConnectionID;
    }

    public Long getDepartureTime() {
        return departureTime;
    }

    @JsonProperty("departureTime")
    public void setDepartureTime(String departureTime) {
        try {
            this.departureTime = SeatOverviewService.dateTimeFormat.parse(departureTime + ":00").getTime();
        } catch (ParseException e) {
            logger.error(e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setDepartureTime(Long departureTime) {
        this.departureTime = departureTime;
    }

    public Long getArrivalTime() {
        return arrivalTime;
    }

    @JsonProperty("arrivalTime")
    public void setArrivalTime(String arrivalTime) {
        try {
            this.arrivalTime = SeatOverviewService.dateTimeFormat.parse(arrivalTime + ":00").getTime();
        } catch (ParseException e) {
            logger.error(e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setArrivalTime(Long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

}
