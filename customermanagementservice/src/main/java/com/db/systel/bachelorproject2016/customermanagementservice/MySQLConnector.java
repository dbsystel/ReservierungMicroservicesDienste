package com.db.systel.bachelorproject2016.customermanagementservice;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.CustomerManagementDAO;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.JdbcCustomerManagementDAO;

public class MySQLConnector implements Runnable {

    @Override
    public void run() {

        while (!CustomerManagementService.connectedToMySQL) {
            connect("customer_database", 3306);

        }

    }

    //Stellt die Verbindung auf Grundlage der durch Consul vermittelten Daten her
    public void connect(String mysqlIP, Integer mysqlPort) {

        DriverManagerDataSource src = new DriverManagerDataSource();

        src.setUsername("customer");
        src.setPassword("customer");
        src.setUrl("jdbc:mysql://" + mysqlIP + ":" + mysqlPort + "/customermanagement?useSSL=false");
        src.setDriverClassName("com.mysql.jdbc.Driver");

        CustomerManagementService.customerManagementDAO = (CustomerManagementDAO) new JdbcCustomerManagementDAO();
        CustomerManagementService.customerManagementDAO.setDataSource(src);

        CustomerManagementService.connectedToMySQL = true;

    }

}
