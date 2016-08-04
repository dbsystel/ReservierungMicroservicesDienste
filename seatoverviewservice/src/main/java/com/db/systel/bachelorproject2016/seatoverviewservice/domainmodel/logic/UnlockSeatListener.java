package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import java.io.UnsupportedEncodingException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class UnlockSeatListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(UnlockSeatListener.class);

    @Override
    public void onMessage(Message message) {

        try {

            //Könnte man wahrscheinlich sogar besser über einen Object Mapper machen
            JSONObject allocation = new JSONObject(new String(message.getBody(), "UTF-8"));

            System.out.println("Received message to unlock: " + message.toString());

            EventStoreConnection.unlockSeat(allocation);

        } catch (JSONException | UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}