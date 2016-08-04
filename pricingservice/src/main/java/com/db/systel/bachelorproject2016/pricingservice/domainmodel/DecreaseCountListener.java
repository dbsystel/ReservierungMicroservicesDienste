package com.db.systel.bachelorproject2016.pricingservice.domainmodel;

import java.io.UnsupportedEncodingException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class DecreaseCountListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(DecreaseCountListener.class);

    @Override
    public void onMessage(Message message) {

        try {
            JSONObject allocation = new JSONObject(new String(message.getBody(), "UTF-8"));

            System.out.println(allocation.toString());

            EventStoreConnection.decreaseNumberOfSeats(allocation.getString("departureTime"),
                    allocation.getString("arrivalTime"), allocation.getInt("trainConnectionID"));

        } catch (JSONException | UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}