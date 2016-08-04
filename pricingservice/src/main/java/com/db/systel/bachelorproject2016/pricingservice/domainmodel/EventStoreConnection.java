package com.db.systel.bachelorproject2016.pricingservice.domainmodel;

import java.text.ParseException;

import com.db.systel.bachelorproject2016.pricingservice.PricingService;
import com.db.systel.bachelorproject2016.pricingservice.clients.SeatOverviewServiceClient;
import com.lambdaworks.redis.RedisConnection;

public class EventStoreConnection {

    public static RedisConnection<String, String> connection;

    /*
     * Erstellt ein neues Schlüssel-Wert-Paar für Redis auf Grundlage der Platzübersicht
     */
    public static String newValue(String departureTime, String arrivalTime, Integer trainConnectionID) {

        System.out.println("Trying to retrieve new values");

        Integer availableSeats = SeatOverviewServiceClient.getAvailableSeats(trainConnectionID, departureTime,
                arrivalTime);
        Integer allSeats = SeatOverviewServiceClient.getAllSeats(trainConnectionID, departureTime, arrivalTime);

        System.out.println("Setting " + availableSeats + ":" + allSeats);
        return availableSeats + ":" + allSeats;

    }

    /*
     * Konvertiert Ankunfts- und Abfahrtszeit in einen long, damit der schlüssel gefunden werden kann
     * 
     * Wenn es den Key noch nicht gibt, muss er durch die Platzübersicht ermittelt werden
     */
    public static Long convertAndInsert(String departureTime, String arrivalTime, Integer trainConnectionID) {

        try {

            Long time = PricingService.dateTimeFormat.parse(departureTime + ":00").getTime();

            String value = connection.get(trainConnectionID + ":" + time);

            System.out.println("Wert aus redis: " + value);
            if (value == null) {

                /*
                 * Neuen Wert ermitteln und in Redis setzen
                 */
                value = newValue(departureTime, arrivalTime, trainConnectionID);
                connection.set(trainConnectionID + ":" + time, value);
            }

            return time;

        } catch (ParseException e) {

            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    //Die Funktionen greifen jeweils auf Redis zu und tun eben genau das, was der Funktionsname sagt...
    public static void decreaseNumberOfSeats(String departureTime, String arrivalTime, Integer trainConnectionID) {
        try {
            connection = PricingService.redisClient.connect();
            Long time = convertAndInsert(departureTime, arrivalTime, trainConnectionID);
            String value = connection.get(trainConnectionID + ":" + time);
            Integer availableSeats = Integer.parseInt(value.split(":")[0]) - 1;
            connection.set(trainConnectionID + ":" + time, availableSeats.toString() + ":" + value.split(":")[1]);
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void increaseNumberOfSeats(String departureTime, String arrivalTime, Integer trainConnectionID) {

        try {
            connection = PricingService.redisClient.connect();
            Long time = convertAndInsert(departureTime, arrivalTime, trainConnectionID);
            String value = connection.get(trainConnectionID + ":" + time);
            Integer availableSeats = Integer.parseInt(value.split(":")[0]) + 1;
            connection.set(trainConnectionID + ":" + time, availableSeats.toString() + ":" + value.split(":")[1]);
            connection.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static String getNumberOfSeats(String departureTime, String arrivalTime, Integer trainConnectionID) {

        connection = PricingService.redisClient.connect();
        Long time = convertAndInsert(departureTime, arrivalTime, trainConnectionID);
        String value = connection.get(trainConnectionID + ":" + time);

        System.out.println("Retrieved from redis: " + value);
        connection.close();
        return value;

    }
}
