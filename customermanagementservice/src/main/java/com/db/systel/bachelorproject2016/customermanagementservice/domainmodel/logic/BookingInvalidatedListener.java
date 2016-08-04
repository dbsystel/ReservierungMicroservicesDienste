package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic;

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.db.systel.bachelorproject2016.customermanagementservice.CustomerManagementService;

public class BookingInvalidatedListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomerManagementService.class);

    @Override
    public void onMessage(Message message) {

        try {

            //KundenID-auslesen 
            JSONObject customerBooking = new JSONObject(new String(message.getBody(), "UTF-8"));

            //E-Mail-Adresse aus der Datenbank lesen 
            String customerEMail = CustomerManagementService.customerManagementDAO.getEmailAddress(customerBooking
                    .getInt("customerID"));

            //E-Mail Senden
            logger.info("Sending E-Mail to " + customerEMail);

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}