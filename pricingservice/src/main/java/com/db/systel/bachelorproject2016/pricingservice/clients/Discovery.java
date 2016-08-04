package com.db.systel.bachelorproject2016.pricingservice.clients;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Discovery {

    public static String communicate(String service) {

        //Finde einen gegebenen Service
        String URL = "http://172.18.0.1:8500/v1/catalog/service/" + service;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<Void> entity = new HttpEntity<Void>(headers);
        try {

            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            return response.getBody();

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return null;
        }

    }

    /*
     * Consul gibt uns die Services im JSON-Format zurück. Mit Object und dem Parameter "property" kann erstmal
     * irgendwas ausgelesen werden
     * 
     * Konkrete Methoden wie getPort / getAddress nutzen das dann, die Dienste können aber auch einfach direkt diese
     * Methoden nutzen und dann selbst typecasten, wie sie wollen TODO:Load Balancing - immer den lokalen Host
     * bevorzugen?
     */
    public static Object get(String service, String property) {

        String resp = communicate(service);

        try {

            if (resp != null && resp != "") {
                JSONArray array = new JSONArray(resp);

                if (array.length() > 0) {
                    JSONObject obj = (JSONObject) array.get(0);
                    return obj.get(property);
                }
            }

            return null;

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return null;
        }
    }

    public static Integer getPort(String service) {

        return (Integer) get(service, "ServicePort");
    }

    public static String getIP(String service) {

        return (String) get(service, "Address");

    }
}
