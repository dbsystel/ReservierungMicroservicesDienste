/*package seatmanagementservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.db.systel.bachelorproject2016.seatmanagementservice.RabbitConnector;
import com.db.systel.bachelorproject2016.seatmanagementservice.SeatManagementService;
import com.db.systel.bachelorproject2016.seatmanagementservice.api.SeatManagementController;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.BookingQueueConfirmedListener;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.BookingQueueDeletedListener;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.JdbcSeatManagementDAO;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementQueueFeeder;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementDAO;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.ShutdownConnectionListener;
public class SeatManagementDatabaseTest {
    private MockMvc mockMvc;

    @Mock
    private SeatManagementService seatManagementService;

    @InjectMocks
    private SeatManagementController seatManagementController;

    @BeforeClass
    public static void connectionsetup() throws Exception {
        SeatManagementService.dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SeatManagementService.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        DriverManagerDataSource src = new DriverManagerDataSource();

        src.setUsername("seatmanagement");
        src.setPassword("seat");
        src.setUrl("jdbc:mysql://" + "10.43.116.187" + ":" + 3306 + "/seatmanagement?useSSL=false");
        src.setDriverClassName("com.mysql.jdbc.Driver");

        SeatManagementService.seatManagementDAO = (SeatManagementDAO) new JdbcSeatManagementDAO();
        SeatManagementService.seatManagementDAO.setDataSource(src);

        SeatManagementService.connectedToMySQL = true;
        
        SeatManagementService.rabbitConnFactory = new CachingConnectionFactory();
        SeatManagementService.rabbitConnFactory.setHost("10.43.116.187");
        SeatManagementService.rabbitConnFactory.setPort(5672);
        SeatManagementService.rabbitConnFactory.setVirtualHost("host_db");
        SeatManagementService.rabbitConnFactory.setUsername("seatmanagement");
        SeatManagementService.rabbitConnFactory.setPassword("seatmanagement");
        SeatManagementService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

        //Ersstellt Admin und Template für den Zugriff auf die Warteschlangen 

        SeatManagementService.admin = new RabbitAdmin(SeatManagementService.rabbitConnFactory);
        SeatManagementService.template = new RabbitTemplate(SeatManagementService.rabbitConnFactory);

        //Initialisiert die Listener 
        RabbitConnector.initListener("seatmanagement.bookings_cancelled", "bookings_cancelled", "delete",
                new BookingQueueDeletedListener());
        RabbitConnector.initListener("seatmanagement.bookings_confirmed", "bookings_confirmed", "confirm",
                new BookingQueueConfirmedListener());

        SeatManagementService.queueFeeder = new SeatManagementQueueFeeder();
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(seatManagementController).build();
    }
	
	@Test
	public void SeatManagementTests() throws Exception{
	    
	    String jsonRoute = "{}";
        JSONObject jRoute = new JSONObject(jsonRoute);
        
	    this.mockMvc
        .perform(
                post(
                        "/insert?type=route")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                jRoute.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
	    
	    String jsonStation = "{\"name\" : \"test\"}";
        JSONObject jStation = new JSONObject(jsonStation);

        this.mockMvc
        .perform(
                post(
                        "/insert?type=station")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                jStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        this.mockMvc
        .perform(
                post(
                        "/insert?type=station")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                jStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().is(400));
        
        
        String jsonRouteStation = "{\"routeID\" : 1,\"stationID\" : 1,\"stopTime\" : 0,\"platform\" : 1}";
        JSONObject jRouteStation = new JSONObject(jsonRouteStation);

        this.mockMvc
        .perform(
                post(
                        "/insert?type=route_station")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                jRouteStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        this.mockMvc
        .perform(
                post(
                        "/insert?type=route_station")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                jRouteStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().is(400));
        
        jsonRouteStation = "{\"routeID\" : 100,\"stationID\" : 1000,\"stopTime\" : 12,\"platform\" : 1}";
        jRouteStation = new JSONObject(jsonRouteStation);
        
           this.mockMvc
            .perform(
                    post(
                            "/insert?type=route_station")
                            .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                    jRouteStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
            .andExpect(status().is(400));
           
           String jsonTrain = "{\"trainNumber\" : 1234, \"trainCategory\": \"IC\"}";
           JSONObject jTrain = new JSONObject(jsonTrain);

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrain.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrain.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           
           String jsonWagon = "{\"state\" : \"test\"}";
           JSONObject jWagon = new JSONObject(jsonWagon);

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=wagon")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jWagon.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonSeat = "{\"seatClass\": 1, \"seatArea\": \"Ruhe\",\"seatLocation\": \"Fenster\", \"seatCompartmentType\": \"Grossraum\",\"upperLevel\": false,\"wagonID\": 1,\"state\": \"test\"}";
           JSONObject jSeat = new JSONObject(jsonSeat);

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=seat")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jSeat.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonTrainConnection = "{\"routeID\": 1,\"trainID\": 1,\"departureTime\": \"10:40\", \"dayOfWeek\": \"monday\"}";
           JSONObject jTrainConnection = new JSONObject(jsonTrainConnection);
           
           

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           jsonTrainConnection = "{\"routeID\": 100,\"trainID\": 100,\"departureTime\": \"10:40\", \"dayOfWeek\": \"monday\"}";
           jTrainConnection = new JSONObject(jsonTrainConnection);
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           String jsonSeatAllocation = "{\"seatID\": 1,\"trainConnectionID\": 1,\"departureTime\": \"01-02-2015 10:20\",\"arrivalTime\": \"01-02-2015 10:40\"}";
           JSONObject jSeatAllocation = new JSONObject(jsonSeatAllocation);
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=seat_allocation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jSeatAllocation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonWagonTrainConnection = "{\"wagonID\": 1,\"trainConnectionID\": 1,\"number\": 3}";
           JSONObject jWagonTrainConnection = new JSONObject(jsonWagonTrainConnection);

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jWagonTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jWagonTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           jsonWagonTrainConnection = "{\"wagonID\": 100,\"trainConnectionID\": 100,\"number\": 3}";
           jWagonTrainConnection = new JSONObject(jsonWagonTrainConnection);
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jWagonTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           String jsonTrainConnectionCancellation = "{\"trainConnectionID\" : 1,\"day\" : \"30-05-2016\"}";
           JSONObject jTrainConnectionCancellation = new JSONObject(jsonTrainConnectionCancellation);

           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnectionCancellation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnectionCancellation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           jsonTrainConnectionCancellation = "{\"trainConnectionID\": 201,\"day\": \"01-05-2015\"}";
           jTrainConnectionCancellation = new JSONObject(jsonTrainConnectionCancellation);
           
           this.mockMvc
           .perform(
                   post(
                           "/insert?type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jTrainConnectionCancellation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           String jsonUpdateStation = "{\"name\" : \"test-update\"}";
           JSONObject jUpdateStation = new JSONObject(jsonUpdateStation);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateRoutStation = "{\"routeID\": 1,\"stationID\": 1,\"stopTime\": 1,\"platform\": 1000}";
           JSONObject jUpdateRoutStation = new JSONObject(jsonUpdateRoutStation);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=route_station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateRoutStation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateTrain = "{\"trainNumber\" : 12345, \"trainCategory\": \"ICE\"}";
           JSONObject jUpdateTrain = new JSONObject(jsonUpdateTrain);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=train")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateTrain.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateWagon = "{\"state\" : \"test-update\"}";
           JSONObject jUpdateWagon = new JSONObject(jsonUpdateWagon);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=wagon")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateWagon.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateSeat = "{\"seatClass\": 2, \"seatArea\": \"Handy\",\"seatLocation\": \"Gang\", \"seatCompartmentType\": \"Abteil\",\"upperLevel\": true,\"wagonID\": 1,\"state\": \"test-update\"}";
           JSONObject jUpdateSeat = new JSONObject(jsonUpdateSeat);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=seat")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateSeat.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateTrainConnection = "{\"routeID\": 1,\"trainID\": 1,\"departureTime\": \"11:44\", \"dayOfWeek\": \"tuesday\"}";
           JSONObject jUpdateTrainConnection = new JSONObject(jsonUpdateTrainConnection);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateSeatAllocation = "{\"seatID\": 1,\"trainConnectionID\": 1,\"departureTime\": \"01-02-2015 11:20\",\"arrivalTime\": \"01-02-2015 11:40\"}";
           JSONObject jUpdateSeatAllocation = new JSONObject(jsonUpdateSeatAllocation);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=seat_allocation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateSeatAllocation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateWagonTrainConnection = "{\"wagonID\": 1,\"trainConnectionID\": 1,\"number\": 2}";
           JSONObject jUpdateWagonTrainConnection = new JSONObject(jsonUpdateWagonTrainConnection);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateWagonTrainConnection.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           String jsonUpdateTrainConnectionCancellation = "{\"trainConnectionID\": 1,\"day\": \"02-05-2015\"}";
           JSONObject jUpdateTrainConnectionCancellation = new JSONObject(jsonUpdateTrainConnectionCancellation);

           this.mockMvc
           .perform(
                   post(
                           "/update?id=1&type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(
                                   jUpdateTrainConnectionCancellation.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/disable?id=1&type=wagon")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/disable?id=1&type=seat")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=seat_allocation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=seat")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=wagon")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=route_station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=route")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train_connection_cancellation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=wagon_train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=seat_allocation")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train_connection")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=seat")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=wagon")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=train")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=route_station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=station")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
           
           this.mockMvc
           .perform(
                   post(
                           "/delete?id=1&type=route")
                           .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
           .andExpect(status().is(400));
	}
}*/
