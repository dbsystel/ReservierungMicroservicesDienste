package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.SeatAllocation;

public class BookingQueueConfirmedListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingQueueConfirmedListener.class);

    @Override
    public void onMessage(Message message) {

        try {
            //System.out.println(new String(message.getBody(), "UTF-8"));
            logger.info(new String(message.getBody(), "UTF-8"));

            JSONObject alloction = new JSONObject(new String(message.getBody(), "UTF-8"));

            SeatManagementService.seatManagementDAO.insert(
                    "seat_allocation",
                    new SeatAllocation(alloction.getInt("seatID"), alloction.getInt("trainConnectionID"),
                            SeatManagementService.dateTimeFormat.parse(alloction.getString("departureTime") + ":00")
                                    .getTime(), SeatManagementService.dateTimeFormat.parse(
                                    alloction.getString("arrivalTime") + ":00").getTime()));

        } catch (Exception e) {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
}