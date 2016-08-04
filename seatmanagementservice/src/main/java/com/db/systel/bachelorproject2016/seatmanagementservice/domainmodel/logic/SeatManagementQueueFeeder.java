package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;

import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;

public class SeatManagementQueueFeeder {

    private static final Logger logger = LoggerFactory.getLogger(SeatManagementQueueFeeder.class);

    private TopicExchange train_connection;

    private TopicExchange seats;

    private TopicExchange wagon;

    public SeatManagementQueueFeeder() {
        try {

            //Exchanges deklarieren
            train_connection = new TopicExchange("train_connection");
            seats = new TopicExchange("seats");
            wagon = new TopicExchange("wagon");

            SeatManagementService.admin.declareExchange(train_connection);
            SeatManagementService.admin.declareExchange(seats);
            SeatManagementService.admin.declareExchange(wagon);

        } catch (AmqpException e) {
            logger.error(e.getMessage());
        }
    }

    public void cancelTrainConnection(Integer id, String day) {

        try {
            //JSON Objekt aus den Informationen erstellen
            JSONObject obj = new JSONObject();
            obj.put("trainConnectionID", id);
            obj.put("day", day);
            Thread cancel_train_connection = new Thread(new MessageThread(obj, "train_connection", "cancel"));
            cancel_train_connection.start();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void disableSeats(List<Integer> seatIDs) {

        try {
            //JSON Objekt aus den Informationen erstellen
            JSONObject obj = new JSONObject();
            obj.put("seatIDs", seatIDs.get(0));

            for (int i = 1; i < seatIDs.size(); i++) {
                obj.accumulate("seatIDs", seatIDs.get(i));
            }

            Thread disable_seats = new Thread(new MessageThread(obj, "seats", "disable"));
            disable_seats.start();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void disableWagon(Integer wagonID, List<Integer> seatIDs) {

        try {
            //JSON Objekt aus den Informationen erstellen
            JSONObject obj = new JSONObject();
            obj.put("wagonID", wagonID);
            obj.put("seatIDs", seatIDs.get(0));

            for (int i = 1; i < seatIDs.size(); i++) {
                obj.accumulate("seatIDs", seatIDs.get(i));
            }
            Thread disable_wagon = new Thread(new MessageThread(obj, "wagon", "disable"));
            disable_wagon.start();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}