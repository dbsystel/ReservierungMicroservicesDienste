package com.db.systel.bachelorproject2016.pricingservice.clients;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SeatOverviewServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(SeatOverviewServiceClient.class);

    private static RestTemplate restTemplate;

    public static String getInstanceAddress() {

        String URL = "http://172.18.0.1:8500/v1/catalog/service/seatoverviewservice";

        restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);
        try {

            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null && response.getBody() != "") {
                JSONArray array = new JSONArray(response.getBody());

                if (array.length() > 0) {
                    JSONObject obj = (JSONObject) array.get(0);
                    return (obj.getString("Address") + ":" + obj.getString("ServicePort"));
                }
            }

            return null;

        } catch (Exception e) {

            //System.out.println(e);
            logger.error(e.getMessage());

            return null;
        }
    }

    public static Integer getAvailableSeats(Integer trainConnectionID, String departureTime, String arrivalTime) {

        restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        /*
         * Es gibt verschiedene Header, die gesetzt werden müssen. Hier brauchen wir keinen, weil wir nur ein Double
         * wollen. Würden wir aber z.B. JSON-Erwarten, müssten die Header "application/json" heißen.
         * 
         * Ansonsten kann er die Antwort nicht verarbeiten
         * 
         * z.B.: headers.add("Accept", "text/plain"); headers.add("Content-Type", "text/plain");
         * 
         * 
         * ? gibt den Typ des RequestBody an, wenn es einen gäbe (hier haben wir nur ein GET-Statement, da gibt es keine
         * Request-Bodys. Gäbe es einen body, müsste das der erste Parameter sein.
         * 
         * Referenz, wenn du dir das mal anschauen willst, sind die NotificationService-Clients
         */

        headers.add("Accept", "application/json");
        HttpEntity<List<Integer>> entity = new HttpEntity<List<Integer>>(headers);

        //String address = getInstanceAddress();

        //if (address == null) {
        //    return null;
        //}

        String address = "seatoverviewservice:8080";

        String URL = String.format("http://%s/%s", address, "get-seats?trainConnectionID=" + trainConnectionID
                + "&day=" + departureTime.substring(0, 10) + "&departure=" + departureTime.substring(11, 16)
                + "&arrival=" + arrivalTime.substring(11, 16));

        try {

            /*
             * <T> gibt an, was wir zurück erwarten. Bei einem Array z.B. dataType[].class
             */
            System.out.println("Sending to " + URL);

            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            JSONArray array = new JSONArray(response.getBody());

            System.out.println("Retreived: " + array.getJSONObject(0).toString() + " and some other objects");

            System.out.println("Length of array is " + array.length());

            return array.length();

        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

    }

    public static Integer getAllSeats(Integer trainConnectionID, String departureTime, String arrivalTime) {

        restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.add("Accept", "application/json");
        HttpEntity<List<Integer>> entity = new HttpEntity<List<Integer>>(headers);

        String address = "seatoverviewservice:8080";

        String URL = String.format("http://%s/%s", address, "get-all-seats?trainConnectionID=" + trainConnectionID
                + "&day=" + departureTime.substring(0, 10) + "&departure=" + departureTime.substring(11, 16)
                + "&arrival=" + arrivalTime.substring(11, 16));

        try {

            /*
             * <T> gibt an, was wir zurück erwarten. Bei einem Array z.B. dataType[].class
             */

            System.out.println("Sending to " + URL);

            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            JSONArray array = new JSONArray(response.getBody());

            System.out.println("Retreived: " + array.getJSONObject(0).toString() + " and some other objects");

            System.out.println("Length of array is " + array.length());

            return array.length();

        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

    }
}
