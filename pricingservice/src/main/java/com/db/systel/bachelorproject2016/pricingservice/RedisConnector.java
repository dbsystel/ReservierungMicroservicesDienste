package com.db.systel.bachelorproject2016.pricingservice;

import java.net.InetAddress;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class RedisConnector implements Runnable {

    @Override
    public void run() {

        while (!PricingService.connectedToRedis) {
            connect("redis_pricing", 6379);
        }

    }

    public void connect(String redisIP, Integer redisPort) {
        try {
            PricingService.redisClient = new RedisClient(RedisURI.create("redis://"
                    + InetAddress.getByName(redisIP).getHostAddress() + ":" + redisPort));
            PricingService.connectedToRedis = true;
        }

        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
