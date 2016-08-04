package com.db.systel.bachelorproject2016.seatoverviewservice;

import java.net.InetAddress;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class RedisConnector implements Runnable {

    @Override
    public void run() {

        while (!SeatOverviewService.connectedToRedis) {
            connect("redis_main_locks", 6379, "redis_ttl_locks", 6379);
        }

    }

    //Die Verbindung wird auf Grundlage der Daten von Consul hergestellt 
    public void connect(String redisIPMain, Integer redisIPortMain, String redisIPTTL, Integer redisIPortTTL) {
        try {
            SeatOverviewService.redisClient = new RedisClient(RedisURI.create("redis://"
                    + InetAddress.getByName(redisIPMain).getHostAddress() + ":" + redisIPortMain));
            SeatOverviewService.redisClientTTL = new RedisClient(RedisURI.create("redis://"
                    + InetAddress.getByName(redisIPTTL).getHostAddress() + ":" + redisIPortTTL));

            SeatOverviewService.connectedToRedis = true;
        } catch (Exception e) {
            SeatOverviewService.logger.error(e.getMessage());
        }

    }

}
