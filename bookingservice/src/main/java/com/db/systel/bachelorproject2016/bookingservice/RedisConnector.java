package com.db.systel.bachelorproject2016.bookingservice;

import java.net.InetAddress;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class RedisConnector implements Runnable {

    @Override
    public void run() {

        while (!BookingService.connectedToRedis) {
            connect("redis_main_guards", 6379, "redis_ttl_guards", 6379);
        }

    }

    public void connect(String redisIPMain, Integer redisIPortMain, String redisIPTTL, Integer redisIPortTTL) {
        try {
            BookingService.redisClient = new RedisClient(RedisURI.create("redis://"
                    + InetAddress.getByName(redisIPMain).getHostAddress() + ":" + redisIPortMain));
            BookingService.redisClientTTL = new RedisClient(RedisURI.create("redis://"
                    + InetAddress.getByName(redisIPTTL).getHostAddress() + ":" + redisIPortTTL));
            BookingService.connectedToRedis = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while tryint to connect to redis: " + e.getMessage());
        }
    }

}
