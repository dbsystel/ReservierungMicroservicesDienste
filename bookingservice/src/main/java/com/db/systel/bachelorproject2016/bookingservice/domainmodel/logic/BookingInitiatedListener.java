package com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.PartialBooking;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BookingInitiatedListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Override
    public void onMessage(Message message) {

        try {

            //Konvertiert den String zur Teilbuchung
            ObjectMapper mapper = new ObjectMapper();
            PartialBooking booking = mapper.readValue(new String(message.getBody(), "UTF-8"), PartialBooking.class);

            EventStoreConnection.removeGuard(booking);

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}