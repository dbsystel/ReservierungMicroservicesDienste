package com.db.systel.bachelorproject2016.seatoverviewservice;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.db.systel.bachelorproject2016.seatoverviewservice.api.SeatOverviewController;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.SeatOverviewDAO;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.SeatoverviewQueueFeeder;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.UnlockSeatListener;
import com.lambdaworks.redis.RedisClient;

@SpringBootApplication
public class SeatOverviewService {

    public static final Logger logger = LoggerFactory.getLogger(SeatOverviewService.class);

    public static SeatOverviewDAO seatOverviewDAO;

    public static RedisClient redisClient;

    public static RedisClient redisClientTTL;

    public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public static CachingConnectionFactory rabbitConnFactory;

    public static RabbitAdmin admin;

    public static RabbitTemplate template;

    public static UnlockSeatListener queueListener;

    //Daten, die angeben, ob eine Verbindung hergestellt werden konnte

    public static SeatoverviewQueueFeeder queueFeeder;

    public static boolean connectedToRabbit;

    public static boolean connectedToMySQL;

    public static boolean connectedToRedis;

    public static void main(String[] args) {

        connectedToRabbit = false;

        connectedToMySQL = false;

        connectedToRedis = false;

        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Startet die Threads, die die Verbindung herstellen 

        //Es werden einzelne Threads verwendet, damit das gesamte Hochfahren nicht blockiert wird 
        Thread connectToSQL = new Thread(new MySQLConnector());
        connectToSQL.start();

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

        Thread connectToRedis = new Thread(new RedisConnector());
        connectToRedis.start();

        SpringApplication.run(SeatOverviewController.class, args);
    }

}
