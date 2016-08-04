package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.SeatAllocation;
import com.lambdaworks.redis.RedisConnection;

public class EventStoreConnection {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreConnection.class);

    public static boolean lockSeat(List<SeatAllocation> seats) {

        RedisConnection<String, String> connection = SeatOverviewService.redisClient.connect();
        RedisConnection<String, String> connectionTTL = SeatOverviewService.redisClientTTL.connect();

        List<SeatAllocation> rollback = new ArrayList<SeatAllocation>();

        try {
            for (SeatAllocation seat : seats) {

                boolean arrivalInserted = true;

                // Wenn nicht inserted werden kann --> Wert schon vorhanden -->
                // rollback
                if (connection.zadd(seat.getSeatID() + ":" + seat.getTrainConnectionID(), seat.getDepartureTime(),
                        seat.getDepartureTime() + ":D") == 0) {
                    arrivalInserted = false;
                    connection.close();
                    connectionTTL.close();
                    unlockSeats(rollback);
                    return false;

                }

                // Set timer to delete
                connectionTTL.set(
                        "DEL/" + seat.getSeatID() + ":" + seat.getTrainConnectionID() + "/" + seat.getDepartureTime()
                                + ":D", Long.toString(System.currentTimeMillis() + 60000L));

                if (connection.zadd(seat.getSeatID() + ":" + seat.getTrainConnectionID(), seat.getArrivalTime(),
                        seat.getArrivalTime() + ":A") == 0) {

                    // Rollback wenn departure schon inserted wurde
                    if (arrivalInserted) {
                        seat.setArrivalTime("01-01-1970 00:00");
                        rollback.add(seat);
                    }
                    connection.close();
                    connectionTTL.close();
                    unlockSeats(rollback);
                    return false;
                }

                // Set timer to delete
                connectionTTL.set(
                        "DEL/" + seat.getSeatID() + ":" + seat.getTrainConnectionID() + "/" + seat.getArrivalTime()
                                + ":A", Long.toString(System.currentTimeMillis() + 60000L));

                rollback.add(seat);

                // Rang / Position suchen
                Long dep = connection.zrank(seat.getSeatID() + ":" + seat.getTrainConnectionID(),
                        seat.getDepartureTime() + ":D");
                Long arr = connection.zrank(seat.getSeatID() + ":" + seat.getTrainConnectionID(),
                        seat.getArrivalTime() + ":A");

                boolean A = true;

                dep--;

                if (dep < 0) {
                    dep = 0L;
                    A = false;
                }

                List<String> stops = connection.zrange(seat.getSeatID() + ":" + seat.getTrainConnectionID(), dep,
                        arr + 1);

                for (String stop : stops) {
                    if (A) {
                        if (!stop.endsWith("A")) {
                            connectionTTL.close();
                            connection.close();
                            unlockSeats(rollback);
                            return false;
                        }
                    } else {
                        if (!stop.endsWith("D")) {

                            connectionTTL.close();
                            connection.close();
                            unlockSeats(rollback);
                            return false;
                        }
                    }
                    A = !A;
                }

            }

            connectionTTL.close();
            connection.close();

            return true;

        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    public static boolean unlockSeats(List<SeatAllocation> seats) {

        RedisConnection<String, String> connection = SeatOverviewService.redisClient.connect();
        RedisConnection<String, String> connectionTTL = SeatOverviewService.redisClientTTL.connect();

        try {

            // Alle Plätze entfernen
            for (SeatAllocation seat : seats) {

                connection.zrem(seat.getSeatID() + ":" + seat.getTrainConnectionID(), seat.getDepartureTime() + ":D");
                connection.zrem(seat.getSeatID() + ":" + seat.getTrainConnectionID(), seat.getArrivalTime() + ":A");

                connectionTTL.del(
                        "DEL/" + seat.getSeatID() + ":" + seat.getTrainConnectionID() + "/" + seat.getDepartureTime()
                                + ":D",
                        "DEL/" + seat.getSeatID() + ":" + seat.getTrainConnectionID() + "/" + seat.getArrivalTime()
                                + ":A");

            }
            connectionTTL.close();
            connection.close();

            return true;

        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }

    }

    public static boolean unlockSeat(JSONObject alloc) {

        RedisConnection<String, String> connection = SeatOverviewService.redisClient.connect();

        try {
            connection.zrem(alloc.getInt("seatID") + ":" + alloc.getInt("trainConnectionID"),
                    alloc.getLong("departureTime") + ":D");
            connection.zrem(alloc.getInt("seatID") + ":" + alloc.getInt("trainConnectionID"),
                    alloc.getLong("arrivalTime") + ":A");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        connection.close();

        return true;

    }
}
