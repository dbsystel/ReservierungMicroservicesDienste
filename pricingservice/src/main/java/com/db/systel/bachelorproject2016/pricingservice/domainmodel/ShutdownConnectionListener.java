package com.db.systel.bachelorproject2016.pricingservice.domainmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;

import com.db.systel.bachelorproject2016.pricingservice.PricingService;
import com.db.systel.bachelorproject2016.pricingservice.RabbitConnector;

public class ShutdownConnectionListener implements ConnectionListener {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownConnectionListener.class);

    public void onCreate(Connection connection) {
        //System.out.println("Connected");
        logger.info("Connected");
        PricingService.connectedToRabbit = true;

    }

    public void onClose(Connection connection) {
        //System.out.println("Lost connection. Trying to re-establish");
        logger.info("Lost connection. Trying to re-establish");
        PricingService.connectedToRabbit = false;

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();
    }

}
