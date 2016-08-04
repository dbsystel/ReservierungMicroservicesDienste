package com.db.systel.bachelorproject2016.bookingservice;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.db.systel.bachelorproject2016.bookingservice.api.BookingController;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.BookingQueueFeeder;
import com.lambdaworks.redis.RedisClient;

@SpringBootApplication
public class BookingService {

    public static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    public static SimpleDateFormat dateFormat;

    public static RedisClient redisClient;

    public static RedisClient redisClientTTL;

    public static CachingConnectionFactory rabbitConnFactory;

    public static RabbitAdmin admin;

    public static RabbitTemplate template;

    public static BookingQueueFeeder queueFeeder;

    public static Session session;

    public static Cluster cluster;

    public static boolean connectedToRabbit;

    public static boolean connectedToRedis;

    public static boolean connectedToCassandra;

    public static void main(String args[]) {

        connectedToRabbit = false;

        connectedToRedis = false;

        connectedToCassandra = false;

        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        //Thread starten, um die Verbindung herzustellen

        //Wird in separierten Threads gestartet, damit die Verbindung die ganze Zeit versucht wird herzustellen, ohne, dass das Startup selbst herausgezögert wird 

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

        Thread connectToRedis = new Thread(new RedisConnector());
        connectToRedis.start();

        Thread connectToCassandra = new Thread(new CassandraConnector());
        connectToCassandra.start();

        SpringApplication.run(BookingController.class, args);

    }
}