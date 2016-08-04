package com.db.systel.bachelorproject2016.seatmanagementservice;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.JdbcSeatManagementDAO;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementDAO;

public class MySQLConnector implements Runnable {

    @Override
    public void run() {

        while (!SeatManagementService.connectedToMySQL) {
            connect("seat_database", 3306);
        }

    }

    //Die Verbindung wird mit den durch Consul vermittelten Adressen hergestellt 
    public void connect(String mysqlIP, Integer mysqlPort) {

        DriverManagerDataSource src = new DriverManagerDataSource();

        src.setUsername("seatmanagement");
        src.setPassword("seat");
        src.setUrl("jdbc:mysql://" + mysqlIP + ":" + mysqlPort + "/seatmanagement?useSSL=false");
        src.setDriverClassName("com.mysql.jdbc.Driver");

        SeatManagementService.seatManagementDAO = (SeatManagementDAO) new JdbcSeatManagementDAO();
        SeatManagementService.seatManagementDAO.setDataSource(src);

        SeatManagementService.connectedToMySQL = true;

    }

}
