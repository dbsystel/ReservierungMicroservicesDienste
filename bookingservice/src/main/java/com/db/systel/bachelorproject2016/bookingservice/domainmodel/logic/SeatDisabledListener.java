package com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.CustomerBooking;

public class SeatDisabledListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Override
    public void onMessage(Message message) {

        try {

            JSONObject msg = new JSONObject(new String(message.getBody(), "UTF-8"));

            String seatIDs = msg.getString("seatIDs");

            seatIDs = seatIDs.replace("[", "");
            seatIDs = seatIDs.replace("]", "");

            String[] split = seatIDs.split(",");

            List<Integer> ids = new ArrayList<Integer>();

            for (String s : split) {
                ids.add(Integer.parseInt(s));
            }
            //Buchung invalidieren
            List<CustomerBooking> customerBookings = CassandraConnection.updateDisabledBookings(ids);

            //Ereignis veröffentlichen 

            for (CustomerBooking cb : customerBookings) {
                BookingService.queueFeeder.bookingInvalidate(cb);
            }

        } catch (IOException | JSONException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}