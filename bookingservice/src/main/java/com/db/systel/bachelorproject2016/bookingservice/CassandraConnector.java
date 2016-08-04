package com.db.systel.bachelorproject2016.bookingservice;

import java.net.InetSocketAddress;

import com.datastax.driver.core.Cluster;

public class CassandraConnector implements Runnable {
    @Override
    public void run() {

        while (!BookingService.connectedToCassandra) {
            connect("cassandra", 9042);
        }

    }

    public void connect(String node, Integer port) {

        try {
            InetSocketAddress addr = new InetSocketAddress(node, port);

            BookingService.cluster = Cluster.builder().addContactPointsWithPorts(new InetSocketAddress[] { addr })
                    .build();
            BookingService.session = BookingService.cluster.connect("booking");

            BookingService.connectedToCassandra = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while tryint to connect to cassandra: " + e.getMessage());
        }

    }
}
