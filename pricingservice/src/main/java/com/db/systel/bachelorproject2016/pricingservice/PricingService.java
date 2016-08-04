package com.db.systel.bachelorproject2016.pricingservice;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.db.systel.bachelorproject2016.pricingservice.api.PricingController;
import com.lambdaworks.redis.RedisClient;

@SpringBootApplication
public class PricingService {

    public static RedisClient redisClient;

    public static boolean connectedToRedis;

    public static boolean connectedToRabbit;

    public static SimpleDateFormat dateTimeFormat;

    public static CachingConnectionFactory rabbitConnFactory;

    public static RabbitAdmin admin;

    public static RabbitTemplate template;

    public static void main(String args[]) {

        connectedToRedis = false;

        connectedToRabbit = false;

        dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Thread connectToRedis = new Thread(new RedisConnector());
        connectToRedis.start();

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

        SpringApplication.run(PricingController.class, args);
    }
}
