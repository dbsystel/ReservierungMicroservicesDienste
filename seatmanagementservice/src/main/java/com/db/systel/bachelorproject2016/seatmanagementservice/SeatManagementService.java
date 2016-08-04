package com.db.systel.bachelorproject2016.seatmanagementservice;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;

import com.db.systel.bachelorproject2016.seatmanagementservice.api.SeatManagementController;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementQueueFeeder;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementDAO;

public class SeatManagementService {

    public static final Logger logger = LoggerFactory.getLogger(SeatManagementService.class);

    public static SeatManagementDAO seatManagementDAO;

    public static CachingConnectionFactory rabbitConnFactory;

    public static RabbitAdmin admin;

    public static RabbitTemplate template;

    public static SimpleDateFormat dateTimeFormat;

    public static SeatManagementQueueFeeder queueFeeder;

    //Informationen darüber, welche Verbindungen hergestellt werden können 

    public static boolean connectedToRabbit;

    public static boolean connectedToMySQL;

    public static boolean connectedToRedis;

    public static void main(String[] args) {

        connectedToRabbit = false;

        connectedToMySQL = false;

        connectedToRedis = false;

        dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Startet die Threads, die die Verbindung herstellen 

        //Es werden einzelne Threads verwendet, damit das gesamte Hochfahren nicht blockiert wird 
        Thread connectToSQL = new Thread(new MySQLConnector());
        connectToSQL.start();

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

        SpringApplication.run(SeatManagementController.class, args);
    }

    //Wird für die Tests benötigt
    public static void initDatetime() {

        dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    }

}
