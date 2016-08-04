package com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.PartialBooking;
import com.lambdaworks.redis.RedisConnection;

public class EventStoreConnection {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreConnection.class);

    public static boolean insertGuard(PartialBooking pb) {

        RedisConnection<String, String> connection = null;
        RedisConnection<String, String> connectionTTL = null;

        try {
            connection = BookingService.redisClient.connect();
            connectionTTL = BookingService.redisClientTTL.connect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Conntected to both redis clients");

        System.out.println("Conntected to both redis clients");

        try {
            Long arrivalTime = BookingService.dateFormat.parse(pb.getArrivalTime() + ":00").getTime();
            Long departureTime = BookingService.dateFormat.parse(pb.getDepartureTime() + ":00").getTime();

            System.out.println("Tried to set departure T");
            if (connection.zadd(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", departureTime,
                    departureTime + ":D") == 0) {
                connectionTTL.close();
                connection.close();
                return false;

            }

            System.out.println("Successfully set departure time");

            // Set timer to delete
            connectionTTL.set("DEL/" + pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking/" + departureTime
                    + ":D", Long.toString(System.currentTimeMillis() + 600000L));

            if (connection.zadd(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", arrivalTime,
                    arrivalTime + ":A") == 0) {

                pb.setArrivalTime("01-01-1970 00:00");
                connectionTTL.close();
                connection.close();
                removeGuard(pb);

                return false;
            }

            System.out.println("Successfully set arrival time");

            // Set timer to delete
            connectionTTL.set("DEL/" + pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking/" + arrivalTime
                    + ":A", Long.toString(System.currentTimeMillis() + 600000L));

            Long dep = connection.zrank(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", departureTime
                    + ":D");
            Long arr = connection.zrank(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", arrivalTime
                    + ":A");

            boolean A = true;

            dep--;

            if (dep < 0) {
                dep = 0L;
                A = false;
            }

            List<String> stops = connection.zrange(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", dep,
                    arr + 1);

            for (String stop : stops) {
                if (A) {
                    if (!stop.endsWith("A")) {
                        connectionTTL.close();
                        connection.close();
                        removeGuard(pb);
                        System.out.println("Check failed");
                        return false;
                    }
                } else {
                    if (!stop.endsWith("D")) {
                        connectionTTL.close();
                        connection.close();
                        removeGuard(pb);
                        System.out.println("Check failed");
                        return false;
                    }
                }
                A = !A;

            }

            connectionTTL.close();
            connection.close();

            System.out.println("Wrote guards. Closed connection.");

            return true;

        } catch (Exception e)

        {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            return false;
        }

    }

    public static boolean removeGuard(PartialBooking pb) {
        RedisConnection<String, String> connection = BookingService.redisClient.connect();
        RedisConnection<String, String> connectionTTL = BookingService.redisClientTTL.connect();

        System.out.println("Encountered Error... Removing guard");
        try {

            Long arrivalTime = BookingService.dateFormat.parse(pb.getArrivalTime() + ":00").getTime();
            Long departureTime = BookingService.dateFormat.parse(pb.getDepartureTime() + ":00").getTime();

            // Remove booking
            connection.zrem(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", departureTime + ":D");
            connection.zrem(pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking", arrivalTime + ":A");

            // Remove message to delete
            connectionTTL.del("DEL/" + pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking/" + departureTime
                    + ":D", "DEL/" + pb.getSeatID() + ":" + pb.getTrainConnectionID() + "Booking/" + arrivalTime,
                    arrivalTime + ":A");

            connection.close();
            connectionTTL.close();

            return true;

        } catch (Exception e) {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            return false;
        }

    }
}
