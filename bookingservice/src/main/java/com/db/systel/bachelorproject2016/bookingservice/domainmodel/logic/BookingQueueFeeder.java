package com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;

import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.CustomerBooking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.PartialBooking;

public class BookingQueueFeeder {

    private static final Logger logger = LoggerFactory.getLogger(BookingQueueFeeder.class);

    private TopicExchange booking;

    public BookingQueueFeeder() {
        try {

            //Deklarieren des Exchanges
            booking = new TopicExchange("booking");
            BookingService.admin.declareExchange(booking);

        } catch (AmqpException e) {

            logger.error(e.getMessage());
        }
    }

    //Starten der Threads zum Befüllen der Queue 
    //Das Befüllen der Queue erfolgt in seperaten Threads, damit die Befüllung andere Prozesse oder weitere Aufrufe nicht blockiert (z.B. wenn die Queue ausfällt)

    //Angabe: Message / Objekt, das Inhalt der Message sein soll; Exchange und Routingschlüssel

    public JSONObject createObjectForBody(PartialBooking pb) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("seatID", pb.getSeatID());
            obj.put("trainConnectionID", pb.getTrainConnectionID());
            obj.put("arrivalTime", pb.getArrivalTime());
            obj.put("departureTime", pb.getDepartureTime());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return obj;
    }

    public JSONObject createObjectForBody(CustomerBooking cb) {

        JSONObject obj = new JSONObject();
        try {
            //KundenID zufügen 
            obj.put("customerID", cb.getCustomerID());

            List<PartialBooking> pbs = cb.getPartialBookings();

            //Teilbuchungen hinzufügen
            if (pbs.size() > 0) {

                obj.put("partialBookings", createObjectForBody(pbs.get(0)));

                for (int i = 1; i < pbs.size(); i++) {
                    obj.accumulate("partialBookings", createObjectForBody(pbs.get(i)));
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return obj;
    }

    public void bookingDelete(PartialBooking pb) {
        Thread delete = new Thread(new MessageThread(createObjectForBody(pb), "booking", "delete"));
        delete.start();

    }

    public void bookingChange(PartialBooking oldPB, PartialBooking newPB) {

        Thread delete_old = new Thread(new MessageThread(createObjectForBody(oldPB), "booking", "delete"));
        delete_old.start();

        Thread confirm_new = new Thread(new MessageThread(createObjectForBody(newPB), "booking", "confirm"));
        confirm_new.start();

    }

    public void bookingConfirm(PartialBooking pb) {

        Thread confirm = new Thread(new MessageThread(createObjectForBody(pb), "booking", "confirm"));
        confirm.start();
    }

    public void bookingCancel(PartialBooking pb) {
        Thread cancel = new Thread(new MessageThread(createObjectForBody(pb), "booking", "cancel"));
        cancel.start();
    }

    public void bookingInitiate(PartialBooking pb) {
        Thread initiate = new Thread(new MessageThread(createObjectForBody(pb), "booking", "initiate"));
        initiate.start();
    }

    public void bookingInvalidate(CustomerBooking pb) {
        Thread initiate = new Thread(new MessageThread(createObjectForBody(pb), "booking", "invalidate"));
        initiate.start();
    }
}