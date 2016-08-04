package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;

import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;

public class MessageThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageThread.class);

    private JSONObject message;

    private String exchange;

    private String key;

    public MessageThread(JSONObject message, String exchange, String key) {
        this.message = message;
        this.exchange = exchange;
        this.key = key;
    }

    @Override
    public void run() {
        try {

            SeatManagementService.template.convertAndSend(exchange, key, message.toString());
            System.out.println("Published message \"" + message.toString() + "\" with key " + key);

        } catch (AmqpException e) {
            logger.error(e.getMessage());
        }
    }
}
