package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import java.util.List;

import javax.sql.DataSource;

import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.Seat;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.TrainConnection;

public interface SeatOverviewDAO {

    public void setDataSource(DataSource dataSource);

    public List<TrainConnection> getTrainConnections(String start, String destination, Long departureTime,
            Integer day, boolean departure, Long timeTillDay);

    public List<Seat> getSeat(Integer trainConnectionID, Integer seatClass, String area, String location,
            String compartmentType, Boolean upperLevel, Integer numberOfPersons, Long departure, Long arrival);

    public List<Seat> getAllAvailableSeats(int trainConnectionID, Long departure, Long arrival);

    public List<Seat> getAllSeats(int trainConnectionID, Long departure, Long arrival);

    public boolean lockSeat(int seatID);

    public boolean unlockSeat(int seatID);
}
