package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;

import com.db.systel.bachelorproject2016.seatoverviewservice.RabbitConnector;
import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;

public class ShutdownConnectionListener implements ConnectionListener {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownConnectionListener.class);

    @Override
    public void onCreate(Connection connection) {
        //System.out.println("Connected");
        logger.info("Connected");
        SeatOverviewService.connectedToRabbit = true;

    }

    @Override
    public void onClose(Connection connection) {
        //System.out.println("Lost connection. Trying to re-establish");
        logger.info("Lost connection. Trying to re-establish");
        SeatOverviewService.connectedToRabbit = false;

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

    }

}
