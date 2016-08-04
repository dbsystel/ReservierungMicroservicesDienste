package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic;

import java.util.List;

import javax.sql.DataSource;

import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.DatabaseObject;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.SeatAllocation;

public interface SeatManagementDAO {

    public void setDataSource(DataSource dataSource);

    // Insert Statements
    /*
     * Returns ID of newly inserted value
     */
    public Integer insert(String type, DatabaseObject databaseObject);

    // Update Statements
    public DatabaseObject update(Integer id, String type, DatabaseObject databaseObject);

    // Delete Statements

    public boolean delete(Integer id, String type);

    // Disabled Statements
    public List<Integer> disable(Integer id, String type);

    public DatabaseObject select(Integer id, String type);

    public boolean deleteSeatAllocation(SeatAllocation seatAllocation);

    public SeatAllocation updateSeatAllocation(SeatAllocation origin, SeatAllocation newInformation);
}
