package com.db.systel.bachelorproject2016.seatmanagementservice.api;

import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.DatabaseObject;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Route;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.RouteStation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Seat;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.SeatAllocation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Station;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Train;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.TrainConnection;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.TrainConnectionCancellation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Wagon;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.WagonTrainConnection;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableAutoConfiguration
@Controller
@Configuration
public class SeatManagementController {

    /*
     * Einige Werte sollen durch den Manager in menschlich lesbarer Form eingegeben werden, aber in der Datenbank als
     * Long auftauchen.
     * 
     * Diese Werte werden an dieser Stelle umgewandelt
     */

    private static final Logger logger = LoggerFactory.getLogger(SeatManagementController.class);

    public static JSONObject parseDateTimes(String type, JSONObject json) {

        SeatManagementService.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            switch (type) {

            case "train_connection":

                if (json.has("departureTime")) {

                    String departureTime = json.getString("departureTime");

                    json.remove("departureTime");
                    json.put("departureTime",
                            SeatManagementService.dateTimeFormat.parse("01-01-1970 " + departureTime + ":00")
                                    .getTime());

                }

                if (json.has("dayOfWeek")) {

                    String dayOfWeek = json.getString("dayOfWeek");
                    json.remove("dayOfWeek");

                    String[] weekdays = new String[] { "monday", "tuesday", "wednesday", "thursday", "friday",
                            "saturday", "sunday" };
                    json.put("dayOfWeek", Arrays.asList(weekdays).indexOf(dayOfWeek) + 1);

                }

                break;

            case "seat_allocation":

                if (json.has("departureTime")) {

                    String departureTime = json.getString("departureTime");
                    json.remove("departureTime");
                    json.put("departureTime", SeatManagementService.dateTimeFormat.parse(departureTime + ":00")
                            .getTime());

                }
                if (json.has("arrivalTime")) {

                    String arrivalTime = json.getString("arrivalTime");
                    json.remove("arrivalTime");
                    json.put("arrivalTime", SeatManagementService.dateTimeFormat.parse(arrivalTime + ":00").getTime());
                }

                break;

            case "route_station":
                /*
                 * Kommt als Minutenzahl rein, also einfach 60 s / min * 1000 ms / s
                 */
                if (json.has("stopTime")) {

                    String stopTime = json.getString("stopTime");

                    json.remove("stopTime");
                    json.put("stopTime", Integer.parseInt(stopTime) * 60 * 1000);
                }

                break;

            case "train_connection_cancellation":

                if (json.has("day")) {
                    String day = json.getString("day");
                    json.remove("day");
                    json.put("day", SeatManagementService.dateTimeFormat.parse(day + " 00:00:00").getTime());
                }

                break;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

        return json;
    }

    /*
     * Alle Objekte leiten von einem Datenbankobjekt ab. Das ermöglicht es, sich erst später auf einen konkreten
     * Datentyp festzulegen und die Statements schön zu kapseln
     */
    private static DatabaseObject parseObject(String type, String jsonString) {
        DatabaseObject obj = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /*
         * Der Mapper funktioniert von selbst (also benötigt keine Implementierung durch uns). An dieser Stelle ggf.
         * einfach einen neuen Case hinzufügen, wenn noch ein Datenobjekt dazu kommt
         */
        try {
            switch (type) {
            case "route":
                obj = mapper.readValue(jsonString, Route.class);
                break;
            case "station":
                obj = mapper.readValue(jsonString, Station.class);
                break;
            case "route_station":
                obj = mapper.readValue(jsonString, RouteStation.class);
                break;
            case "seat":
                obj = mapper.readValue(jsonString, Seat.class);
                break;
            case "wagon":
                obj = mapper.readValue(jsonString, Wagon.class);
                break;
            case "train":
                obj = mapper.readValue(jsonString, Train.class);
                break;
            case "train_connection":
                obj = mapper.readValue(jsonString, TrainConnection.class);
                break;
            case "wagon_train_connection":
                obj = mapper.readValue(jsonString, WagonTrainConnection.class);
                break;
            case "seat_allocation":
                obj = mapper.readValue(jsonString, SeatAllocation.class);
                break;
            case "train_connection_cancellation":
                obj = mapper.readValue(jsonString, TrainConnectionCancellation.class);
                break;
            default:
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return obj;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> home() {
        return new ResponseEntity<String>("I am a SeatManagementService.", HttpStatus.OK);
    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public ResponseEntity<?> insert(@RequestParam(value = "type") String type, @RequestBody String input) {

        //System.out.println("Received request to insert. Type: " + type);
        logger.info("Received request to insert. Type: " + type);

        try {

            // Gibt die ID des neu hinzugefügten Elements zurück
            Integer insertionID = null;

            //Objekt in ein Datenbankobjekt parsen
            JSONObject json = new JSONObject(input);

            // Tag in lesbarer Form für Weiterleitung an NotificationService speichern
            String day = null;
            if (json.has("day")) {
                day = json.getString("day");
            }

            // Für manche Eingaben müssen die Werte noch in das Long-Format für die Datenbank umgewandelt werden
            json = parseDateTimes(type, json);

            DatabaseObject obj = parseObject(type, json.toString());

            // Wenn das möglich war, dann dieses Objekt einfügen. Die anderen Funktionen machen das analog. Die
            // Datenbank entscheidet dann, wo sie das Objekt einfügt
            if (obj != null) {

                insertionID = SeatManagementService.seatManagementDAO.insert(type, obj);

                if (insertionID != null) {

                    // Wenn eine Zugverbindung gecancelled wurde --> veröffentliche eine Nachricht
                    if (type.equals("train_connection_cancellation")) {

                        SeatManagementService.queueFeeder.cancelTrainConnection(
                                ((TrainConnectionCancellation) obj).getTrainConnectionID(), day);

                    }

                    JSONObject response = new JSONObject();
                    response.put("id", insertionID);
                    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
                }
            }

            return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // TODO: Welchen HTTPStatus?
            return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<?> update(@RequestParam(value = "id") int id, @RequestParam(value = "type") String type,
            @RequestBody String input) {
        //System.out.println("Received request to update. Type: " + type + ", Id: " + id);
        logger.info("Received request to update. Type " + type + ", Id: " + id);

        try {

            JSONObject json = new JSONObject(input);

            json = parseDateTimes(type, json);

            DatabaseObject obj = parseObject(type, json.toString());

            if (obj != null) {

                DatabaseObject newObject = SeatManagementService.seatManagementDAO.update(id, type, obj);
                if (newObject != null) {
                    return new ResponseEntity<DatabaseObject>(newObject, HttpStatus.OK);
                }
                //System.out.println("ID not present");
                logger.info("ID not present");
                return new ResponseEntity<String>("ID not present", HttpStatus.BAD_REQUEST);

            }
            return new ResponseEntity<String>("Object cannot be parsed correctly", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            //System.out.println("Could not update, message:" + e.getMessage());
            logger.error("Could not update, message: " + e.getMessage());
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseEntity<String> delete(@RequestParam(value = "id") int id, @RequestParam(value = "type") String type) {

        try {

            //System.out.println("Received request to delete. Type: " + type + ", Id: " + id);
            logger.info("Received request to delete. Type: " + type + ", Id: " + id);
            if (SeatManagementService.seatManagementDAO.delete(id, type)) {
                return new ResponseEntity<String>("Successfully deleted", HttpStatus.OK);
            }

            //System.out.println("ID not present");
            logger.info("ID not present");
            return new ResponseEntity<String>("ID not present", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            //System.out.println("Could not delete");
            logger.error("Could not delelete: " + e.getMessage());
            return new ResponseEntity<String>("Could not delelete: " + e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public ResponseEntity<?> update(@RequestParam(value = "id") int id, @RequestParam(value = "type") String type) {

        try {

            /*
             * Beim disable kommt eine Liste mit allen betroffenen Plätzen zurück. Diese muss an den
             * Benachrichtigungsdienst weiter geleitet werden, damit Kunden informiert werden
             */
            logger.info("Received request to disable. Type: " + type + ", Id: " + id);

            List<Integer> seatIDs = SeatManagementService.seatManagementDAO.disable(id, type);

            //Veröffentlicht eine Nachricht. Wenn der Wagen ausgefallen ist, wird die gesamte Wagennummer mitgeliefert 
            if (type.equals("wagon")) {
                SeatManagementService.queueFeeder.disableWagon(id, seatIDs);
            } else {
                SeatManagementService.queueFeeder.disableSeats(seatIDs);
            }

            return new ResponseEntity<List<Integer>>(seatIDs, HttpStatus.OK);

        } catch (Exception e) {

            //System.out.println("Failed to disable. Error message was: " + e.getMessage());
            logger.error("Failed to disable. Error message was: " + e.getMessage());

            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @RequestMapping(value = "/change-seat-allocation", method = RequestMethod.POST)
    public ResponseEntity<?> seatAllocationUpdate(@RequestParam(value = "type") String type, @RequestParam(
            value = "trainConnectionID", required = false) Integer trainConnectionID, @RequestParam(value = "seatID",
            required = false) Integer seatID,
            @RequestParam(value = "arrivalTime", required = false) String arrivalTime, @RequestParam(
                    value = "departureTime", required = false) String departureTime, @RequestBody String input)
            throws JSONException {

        JSONObject json = new JSONObject(input);

        json = parseDateTimes("seat_allocation", json);

        SeatAllocation seatAllocation = (SeatAllocation) parseObject("seat_allocation", json.toString());

        /*
         * Wenn das möglich war, dann dieses Objekt einfügen. Die anderen Funktionen machen das analog. Die Datenbank
         * entscheidet dann, wo sie das Objekt einfügt
         */
        if (seatAllocation != null) {

            if (type.equals("delete")) {

                if (SeatManagementService.seatManagementDAO.deleteSeatAllocation(seatAllocation)) {

                    //System.out.println("Deleted Seat Alloction");
                    logger.info("deleted Seat Allocation");
                    return new ResponseEntity<String>("Deleted Seat Allocation", HttpStatus.OK);
                } else {
                    //System.out.println("Was requested to delete the following seat allocation "
                    //        + seatAllocation.toString() + ", but couldn't find a matching entry");
                    logger.info("Was requested to delete the following seat allocation " + seatAllocation.toString()
                            + ", but couldn't find a matching entry");
                    return new ResponseEntity<String>("Such an allocation does not exist", HttpStatus.BAD_REQUEST);
                }
            } else if (type.equals("update")) {
                JSONObject newInformationJSON = new JSONObject();
                newInformationJSON.put("trainConnectionID", trainConnectionID);
                newInformationJSON.put("seatID", seatID);
                newInformationJSON.put("arrivalTime", arrivalTime);
                newInformationJSON.put("departureTime", departureTime);

                newInformationJSON = parseDateTimes("seat_allocation", newInformationJSON);

                DatabaseObject obj = parseObject("seat_allocation", newInformationJSON.toString());

                if (obj != null) {

                    SeatAllocation newObject = SeatManagementService.seatManagementDAO.updateSeatAllocation(
                            seatAllocation, (SeatAllocation) obj);
                    if (newObject != null) {

                        //System.out.println("Updated Seat Allocation");
                        logger.info("Updated Seat Allocation");
                        return new ResponseEntity<SeatAllocation>(newObject, HttpStatus.OK);
                    }

                    //System.out.println("Was requested to update the following seat allocation "
                    //        + seatAllocation.toString() + ", but couldn't find a matching entry");
                    logger.info("Was requested to update the following seat allocation " + seatAllocation.toString()
                            + ", but couldn't find a matching entry");
                    return new ResponseEntity<String>("Such an allocation does not exist", HttpStatus.BAD_REQUEST);

                }

                return new ResponseEntity<String>("Object cannot be parsed correctly", HttpStatus.BAD_REQUEST);

            } else {
                //System.out.println("Received seal allocation request with type " + type + ". This type is unknown");
                logger.info("Received seal allocation request with type " + type + ". This type is unknown");
                return new ResponseEntity<String>("Type " + type + " is invalid. Use update or delete",
                        HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<String>("Object cannot be parsed correctly", HttpStatus.BAD_REQUEST);

    }
}