package com.db.systel.bachelorproject2016.seatoverviewservice;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.JdbcSeatOverviewDAO;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.SeatOverviewDAO;

public class MySQLConnector implements Runnable {

    @Override
    public void run() {

        while (!SeatOverviewService.connectedToMySQL) {
            connect("seat_database_copy", 3306);
        }

    }

    //Zum Herstellen der Verbindung werden die von Consul vermittelten Daten verwendet 
    public void connect(String mysqlIP, Integer mysqlPort) {
        try {
            DriverManagerDataSource src = new DriverManagerDataSource();

            src.setUsername("seatoverview");
            src.setPassword("seat");
            src.setUrl("jdbc:mysql://" + mysqlIP + ":" + mysqlPort + "/seatmanagement?useSSL=false");
            src.setDriverClassName("com.mysql.jdbc.Driver");

            SeatOverviewService.seatOverviewDAO = (SeatOverviewDAO) new JdbcSeatOverviewDAO();
            SeatOverviewService.seatOverviewDAO.setDataSource(src);

            SeatOverviewService.connectedToMySQL = true;

        } catch (Exception e) {
            System.out.println("Failed to connect to mysql " + e.getMessage());
        }

    }

}
