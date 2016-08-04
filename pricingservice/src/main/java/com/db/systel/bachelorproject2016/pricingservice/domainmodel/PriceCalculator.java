package com.db.systel.bachelorproject2016.pricingservice.domainmodel;

import java.text.DecimalFormat;

public class PriceCalculator {

    public static float calculatePrice(Seat seat) {

        float price = 0.0f;
        if (seat.getSeatClass() == 2) {
            price = 4.5f;
        }

        return price;
    }

    //Berechnet den Preis dynamisch aus dem Verhältnis von vorhandenen und freien Plätzen 
    public static String calculateDynamicPrice(Integer seatClass, String departureTime, String arrivalTime,
            Integer trainConnectionID) {

        System.out.println("Received request for dynamic price " + "\ndepartureTime: " + departureTime
                + "\narrivalTime: " + arrivalTime + "\nseatClass" + seatClass + "\ntrainConnectionID"
                + trainConnectionID);

        String value = EventStoreConnection.getNumberOfSeats(departureTime, arrivalTime, trainConnectionID);

        Integer availableSeats = Integer.parseInt(value.split(":")[0]);
        Integer allSeats = Integer.parseInt(value.split(":")[1]);

        float f = 5.0f * allSeats / availableSeats;
        DecimalFormat df = new DecimalFormat("##.##");

        //TODO: nicht ganz so random...
        return df.format(f);

    }
}
