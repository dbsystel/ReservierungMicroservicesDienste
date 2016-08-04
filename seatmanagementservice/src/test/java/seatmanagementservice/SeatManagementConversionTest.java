package seatmanagementservice;

import static org.junit.Assert.assertEquals;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;
import com.db.systel.bachelorproject2016.seatmanagementservice.api.SeatManagementController;

public class SeatManagementConversionTest {

    @Test
    public void testConversionTrainConnectionAM() throws JSONException {

        SeatManagementService.initDatetime();

        JSONObject json = new JSONObject();

        json.put("departureTime", "10:00");
        json.put("dayOfWeek", "monday");

        JSONObject result = SeatManagementController.parseDateTimes("train_connection", json);

        assertEquals(result.getLong("departureTime"), 36000000L);
        assertEquals(result.getLong("dayOfWeek"), 1);
    }

    @Test
    public void testConversionTrainConnectionPM() throws JSONException {

        SeatManagementService.initDatetime();

        JSONObject json = new JSONObject();

        json.put("departureTime", "22:00");

        JSONObject result = SeatManagementController.parseDateTimes("train_connection", json);

        assertEquals(result.getLong("departureTime"), 79200000L);
    }

    @Test
    public void testConversionSeatAllocation() throws JSONException {

        SeatManagementService.initDatetime();

        JSONObject json = new JSONObject();

        json.put("departureTime", "01-04-2015 10:00");
        json.put("arrivalTime", "01-04-2015 10:30");

        JSONObject result = SeatManagementController.parseDateTimes("seat_allocation", json);

        assertEquals(result.getLong("departureTime"), 1427882400000L);
        assertEquals(result.getLong("arrivalTime"), 1427884200000L);
    }

    @Test
    public void testConversionRouteStation() throws JSONException {

        SeatManagementService.initDatetime();

        JSONObject json = new JSONObject();

        json.put("stopTime", "10");

        JSONObject result = SeatManagementController.parseDateTimes("route_station", json);

        assertEquals(result.getLong("stopTime"), 600000L);
    }

    @Test
    public void testConversionTrainConnectionCancellation() throws JSONException {

        SeatManagementService.initDatetime();

        JSONObject json = new JSONObject();

        json.put("day", "01-04-2015");

        JSONObject result = SeatManagementController.parseDateTimes("train_connection_cancellation", json);

        assertEquals(result.getLong("day"), 1427846400000L);
    }

}
