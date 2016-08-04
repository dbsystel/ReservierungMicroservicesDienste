package bookingservice;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.datastax.driver.core.Cluster;
import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.CassandraConnector;
import com.db.systel.bachelorproject2016.bookingservice.RabbitConnector;
import com.db.systel.bachelorproject2016.bookingservice.RedisConnector;
import com.db.systel.bachelorproject2016.bookingservice.api.BookingController;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.BookingQueueFeeder;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.ShutdownConnectionListener;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class BookingServiceDataTest {

    private String myJsonString1 = "{\"seatID\": 1,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private String myJsonString2 = "{\"seatID\": 2,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private String myJsonString3 = "{\"seatID\": 3,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private String myJsonString4 = "{\"seatID\": 1,\"trainConnectionID\":1,\"start\": \"D\",\"destination\": \"E\",\"departureTime\": \"28-03-2016 10:30\",\"arrivalTime\": \"28-03-2016 10:40\",\"price\": 4.5}";

    private String myJsonString5 = "{\"seatID\": 4,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private String myJsonString6 = "{\"seatID\": 5,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private String myJsonString7 = "{\"seatID\": 6,\"trainConnectionID\":2,\"start\": \"A\",\"destination\": \"D\",\"departureTime\": \"28-03-2016 10:00\",\"arrivalTime\": \"28-03-2016 10:30\",\"price\": 4.5}";

    private MockMvc mockMvc;

    private JSONObject json1;

    private JSONObject json2;

    private JSONObject json3;

    private JSONObject json4;

    private JSONObject json5;

    private JSONObject json6;

    private JSONObject json7;

    private static JSONObject resultJson;

    private static JSONObject resultJson2;

    private JSONArray array = new JSONArray();

    private JSONArray array2 = new JSONArray();

    private static JSONArray resultArray;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeClass
    public static void connectionsetup() throws Exception {
        try {
            InetSocketAddress addr = new InetSocketAddress("10.43.116.187", 9042);

            BookingService.cluster = Cluster.builder().addContactPointsWithPorts(new InetSocketAddress[] { addr })
                    .build();
            BookingService.session = BookingService.cluster.connect("booking");

            BookingService.connectedToCassandra = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while tryint to connect to cassandra: " + e.getMessage());
        }
        
        try {
            BookingService.redisClient = new RedisClient(RedisURI.create("redis://"
                    //+ InetAddress.getByName("redis_main_guards").getHostAddress() + ":" + 6379));
                    + "10.43.116.187" + ":" + 6381));
            BookingService.redisClientTTL = new RedisClient(RedisURI.create("redis://"
                    //+ InetAddress.getByName("redis_ttl_guards").getHostAddress() + ":" + 6379));
                    + "10.43.116.187" + ":" + 6382));
            BookingService.connectedToRedis = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while tryint to connect to redis: " + e.getMessage());
        }
        
        try {

            //Setzt Host, Port und Nutzer der Factory
            BookingService.rabbitConnFactory = new CachingConnectionFactory();
            BookingService.rabbitConnFactory.setHost("10.43.116.187");
            BookingService.rabbitConnFactory.setPort(5672);
            BookingService.rabbitConnFactory.setVirtualHost("host_db");
            BookingService.rabbitConnFactory.setUsername("booking");
            BookingService.rabbitConnFactory.setPassword("booking");
            BookingService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

            //Stellt den Admin und das Template zum Senden der Nachrichten auf 
            BookingService.admin = new RabbitAdmin(BookingService.rabbitConnFactory);
            BookingService.template = new RabbitTemplate(BookingService.rabbitConnFactory);

            BookingService.queueFeeder = new BookingQueueFeeder();

            BookingService.connectedToRabbit = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while tryint to connect to rabbit: " + e.getMessage());
        }
    }

    @Before
    public void setup() throws Exception {
        BookingService.dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        BookingService.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        MockitoAnnotations.initMocks(this);
        json1 = new JSONObject(myJsonString1);
        json2 = new JSONObject(myJsonString2);
        json3 = new JSONObject(myJsonString3);
        json4 = new JSONObject(myJsonString4);
        json5 = new JSONObject(myJsonString5);
        json6 = new JSONObject(myJsonString6);
        json7 = new JSONObject(myJsonString7);
        array.put(json1);
        array.put(json2);
        array.put(json3);
        array2.put(json5);
        array2.put(json6);
        array2.put(json7);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        int count = 0;
        while (!BookingService.connectedToCassandra && count <= 10) {
            System.out.println("waiting for Cassandra");
            Thread.sleep(10000);
        }
        if(count > 10){
            System.out.println("stoped waiting for Cassandra and not trying to connect again");
        }
        else{
        System.out.println("Connection established");
        }
    }

    @Test
    public void booking() throws Exception {

        this.mockMvc
                .perform(
                        post("/initiate-partial-bookings?customerID=1&paymentMethod=Lastschrift").contentType(
                                MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                array.toString().getBytes("utf-8"))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")));

        this.mockMvc
                .perform(
                        post("/initiate-partial-bookings?customerID=1&paymentMethod=Lastschrift").contentType(
                                MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                array.toString().getBytes("utf-8"))).andExpect(status().is(409))
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")));

        MvcResult result = this.mockMvc
                .perform(
                        post("/confirm-partial-bookings?customerID=1&paymentMethod=Lastschrift").contentType(
                                MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                array.toString().getBytes("utf-8"))).andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        resultJson = new JSONObject(content);
        resultArray = resultJson.getJSONArray("partialBookings");
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < resultArray.length(); i++)
            jsonValues.add(resultArray.getJSONObject(i));
        Collections.sort(jsonValues, new JSONObjectComparator());
        JSONArray sortedResultArray = new JSONArray(jsonValues);

        JSONObject jsonObj = sortedResultArray.getJSONObject(0);
        MvcResult result2 = this.mockMvc
                .perform(
                        post("/change-partial-booking?partialBookingID=" + jsonObj.getString("partialBookingID"))
                                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                        json4.toString().getBytes("utf-8"))).andExpect(status().isOk()).andReturn();
        String content2 = result2.getResponse().getContentAsString();
        resultJson2 = new JSONObject(content2);
        assertEquals(("A"), resultJson2.getString("start"));
        assertEquals(("D"), resultJson2.getString("destination"));
        assertEquals(("28-03-2016 10:00"), resultJson2.getString("departureTime"));
        assertEquals(("28-03-2016 10:30"), resultJson2.getString("arrivalTime"));

        MvcResult getResult = this.mockMvc
                .perform(
                        get("/get-customer-bookings?customerID=1").accept(
                                MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        String getContent = getResult.getResponse().getContentAsString();
        JSONArray getArray = new JSONArray(getContent);
        assertEquals(getArray.length(), 1);
        JSONObject getObject = getArray.getJSONObject(0);
        assertEquals(getObject.get("paymentMethod"), "Lastschrift");
        JSONArray parBookings = getObject.getJSONArray("partialBookings");
        List<JSONObject> jsonValues2 = new ArrayList<JSONObject>();
        for (int i = 0; i < parBookings.length(); i++)
            jsonValues2.add(parBookings.getJSONObject(i));
        Collections.sort(jsonValues2, new JSONObjectComparator());
        JSONArray sortedParBookings = new JSONArray(jsonValues2);
        assertEquals(sortedParBookings.length(), 3);
        JSONObject parBooking1 = sortedParBookings.getJSONObject(0);
        JSONObject parBooking2 = sortedParBookings.getJSONObject(1);
        JSONObject parBooking3 = sortedParBookings.getJSONObject(2);
        assertEquals(("1"), parBooking1.getString("seatID"));
        assertEquals(("1"), parBooking1.getString("trainConnectionID"));
        assertEquals(("D"), parBooking1.getString("start"));
        assertEquals(("E"), parBooking1.getString("destination"));
        assertEquals(("28-03-2016 10:30"), parBooking1.getString("departureTime"));
        assertEquals(("28-03-2016 10:40"), parBooking1.getString("arrivalTime"));
        assertEquals(("4.5"), parBooking1.getString("price"));
        assertEquals(("valid"), parBooking1.getString("state"));

        assertEquals(("2"), parBooking2.getString("seatID"));
        assertEquals(("2"), parBooking2.getString("trainConnectionID"));
        assertEquals(("A"), parBooking2.getString("start"));
        assertEquals(("D"), parBooking2.getString("destination"));
        assertEquals(("28-03-2016 10:00"), parBooking2.getString("departureTime"));
        assertEquals(("28-03-2016 10:30"), parBooking2.getString("arrivalTime"));
        assertEquals(("4.5"), parBooking2.getString("price"));
        assertEquals(("valid"), parBooking2.getString("state"));

        assertEquals(("3"), parBooking3.getString("seatID"));
        assertEquals(("2"), parBooking3.getString("trainConnectionID"));
        assertEquals(("A"), parBooking3.getString("start"));
        assertEquals(("D"), parBooking3.getString("destination"));
        assertEquals(("28-03-2016 10:00"), parBooking3.getString("departureTime"));
        assertEquals(("28-03-2016 10:30"), parBooking3.getString("arrivalTime"));
        assertEquals(("4.5"), parBooking3.getString("price"));
        assertEquals(("valid"), parBooking3.getString("state"));

        JSONObject toDelete1 = sortedResultArray.getJSONObject(1);
        this.mockMvc.perform(
                post("/delete-partial-booking?partialBookingID=" + toDelete1.getString("partialBookingID"))
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                json2.toString().getBytes("utf-8"))).andExpect(status().isOk());
        JSONObject toDelete2 = sortedResultArray.getJSONObject(2);
        this.mockMvc.perform(
                post("/delete-partial-booking?partialBookingID=" + toDelete2.getString("partialBookingID"))
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                json3.toString().getBytes("utf-8"))).andExpect(status().isOk());
        ;
        this.mockMvc.perform(
                post("/delete-partial-booking?partialBookingID=" + resultJson2.getString("partialBookingID"))
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                json4.toString().getBytes("utf-8"))).andExpect(status().isOk());

    }

    @Test
    public void cancelBookings() throws Exception {
        this.mockMvc
                .perform(
                        post("/initiate-partial-bookings?customerID=1&paymentMethod=Lastschrift").contentType(
                                MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                array2.toString().getBytes("utf-8"))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")));

        this.mockMvc.perform(
                post("/cancel-partial-bookings").contentType(
                        MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                        array2.toString().getBytes("utf-8"))).andExpect(status().isOk());
    }

}

class JSONObjectComparator implements Comparator<JSONObject> {

    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        String valA = new String();
        String valB = new String();

        try {
            valA = o1.get("seatID").toString();
            valB = o2.get("seatID").toString();
        } catch (JSONException e) {
            return 0;
        }

        return valA.compareTo(valB);
    }
}
