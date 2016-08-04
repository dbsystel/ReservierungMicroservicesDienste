package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;

import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.SeatAllocation;

public class SeatoverviewQueueFeeder {

    private static final Logger logger = LoggerFactory.getLogger(SeatoverviewQueueFeeder.class);

    private TopicExchange seat;

    public SeatoverviewQueueFeeder() {
        try {

            //Deklarieren des Exchanges
            seat = new TopicExchange("seat");
            SeatOverviewService.admin.declareExchange(seat);

        } catch (AmqpException e) {

            logger.error(e.getMessage());
        }
    }

    //Starten der Threads zum Befüllen der Queue 
    //Das Befüllen der Queue erfolgt in seperaten Threads, damit die Befüllung andere Prozesse oder weitere Aufrufe nicht blockiert (z.B. wenn die Queue ausfällt)

    //Angabe: Message / Objekt, das Inhalt der Message sein soll; Exchange und Routingschlüssel

    public void seatSuggested(SeatAllocation seat_alloc) {

        try {
            JSONObject json = new JSONObject();
            json.put("seatID", seat_alloc.getSeatID());
            json.put("trainConnectionID", seat_alloc.getTrainConnectionID());
            json.put("departureTime", seat_alloc.getDepartureTime());
            json.put("arrivalTime", seat_alloc.getArrivalTime());
            Thread delete = new Thread(new MessageThread(json, "seat", "suggested"));
            delete.start();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void seatChosen(SeatAllocation seat_alloc) {
        try {
            JSONObject json = new JSONObject();
            json.put("seatID", seat_alloc.getSeatID());
            json.put("trainConnectionID", seat_alloc.getTrainConnectionID());
            json.put("departureTime", seat_alloc.getDepartureTime());
            json.put("arrivalTime", seat_alloc.getArrivalTime());
            Thread delete = new Thread(new MessageThread(json, "seat", "chosen"));
            delete.start();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}