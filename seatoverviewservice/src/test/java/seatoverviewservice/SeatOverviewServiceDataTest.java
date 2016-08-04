/*package seatoverviewservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.db.systel.bachelorproject2016.seatoverviewservice.RabbitConnector;
import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;
import com.db.systel.bachelorproject2016.seatoverviewservice.api.SeatOverviewController;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.UnlockSeatListener;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.JdbcSeatOverviewDAO;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.SeatOverviewDAO;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.ShutdownConnectionListener;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;

public class SeatOverviewServiceDataTest {
    private MockMvc mockMvc;

    @Mock
    private SeatOverviewServiceTest seatOverviewService;

    @InjectMocks
    private SeatOverviewController seatOverviewController;

    @BeforeClass
    public static void connectionsetup() throws Exception {
        SeatOverviewService.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        SeatOverviewService.dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DriverManagerDataSource src = new DriverManagerDataSource();

        src.setUsername("seatoverview");
        src.setPassword("seat");
        src.setUrl("jdbc:mysql://" + "10.43.116.187" + ":" + 3309 + "/seatmanagement?useSSL=false");
        src.setDriverClassName("com.mysql.jdbc.Driver");

        SeatOverviewService.seatOverviewDAO = (SeatOverviewDAO) new JdbcSeatOverviewDAO();
        SeatOverviewService.seatOverviewDAO.setDataSource(src);

        SeatOverviewService.rabbitConnFactory = new CachingConnectionFactory();
        SeatOverviewService.rabbitConnFactory.setHost("10.43.116.187");
        SeatOverviewService.rabbitConnFactory.setPort(5672);
        SeatOverviewService.rabbitConnFactory.setVirtualHost("host_db");
        SeatOverviewService.rabbitConnFactory.setUsername("seatoverview");
        SeatOverviewService.rabbitConnFactory.setPassword("seatoverview");
        SeatOverviewService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

        //Deklariert Admin und Template, um mit der MessageQueue zu kommunizieren
        SeatOverviewService.admin = new RabbitAdmin(SeatOverviewService.rabbitConnFactory);
        SeatOverviewService.template = new RabbitTemplate(SeatOverviewService.rabbitConnFactory);

        //Initalisiert den Listener 
        RabbitConnector.initListener("seatoverview.bookings_cancelled", "bookings_cancelled", "*", new UnlockSeatListener());
        
        try {
            SeatOverviewService.redisClient = new RedisClient(RedisURI.create("redis://"
                    + "10.43.116.187" + ":" + 6379));
            SeatOverviewService.redisClientTTL = new RedisClient(RedisURI.create("redis://"
                    + "10.43.116.187" + ":" + 6380));

            SeatOverviewService.connectedToRedis = true;
        } catch (Exception e) {
            SeatOverviewService.logger.error(e.getMessage());
        }
    }
    
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(seatOverviewController).build();
    }
    
    @Test
    public void SeatOverviewTests() throws Exception{

        this.mockMvc
        .perform(
                get(
                        "/get-train-connections?start=A&destination=D&day=21-03-2016&time=10:00&departure=true")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        
        this.mockMvc
        .perform(
                get(
                        "/get-train-connections?start=A&destination=F&day=21-03-2016&time=10:00&departure=true")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().is(404)).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        MvcResult id1 = this.mockMvc
        .perform(
                get(
                        "/get-seats-recommendation?trainConnectionID=1&seatClass=2&numberOfPersons=1&arrival=10:30&departure=10:00&day=21-03-2016")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn();

        
        MvcResult id2= this.mockMvc
        .perform(
                get(
                        "/get-seats-recommendation?trainConnectionID=1&seatClass=2&numberOfPersons=1&arrival=10:30&departure=10:00&day=21-03-2016")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn();
        String sid1 = id1.getResponse().getContentAsString();
        String sid2 = id2.getResponse().getContentAsString();
        JSONArray jids = new JSONArray(sid1);
        JSONObject jid = new JSONObject(jids.get(0).toString());
        sid1 = jid.getString("id");
        jids = new JSONArray(sid2);
        jid = new JSONObject(jids.get(0).toString());
        sid2 = jid.getString("id");
        if(sid1.equals(sid2)){
            fail("same id error");
        }
        String lockbody = "{\"seatID\": " + sid1 + ",\"trainConnectionID\": 1,\"departureTime\":\"21-03-2016 10:00\",\"arrivalTime\":\"21-03-2016 10:30\"}";
        JSONObject jlockbody = new JSONObject(lockbody);
        JSONArray jlockarray = new JSONArray();
        jlockarray.put(jlockbody);
        lockbody = "{\"seatID\": " + sid2 + ",\"trainConnectionID\": 1,\"departureTime\":\"21-03-2016 10:00\",\"arrivalTime\":\"21-03-2016 10:30\"}";
        jlockbody = new JSONObject(lockbody);
        jlockarray.put(jlockbody);
        
        this.mockMvc
        .perform(
                post(
                        "/unlock-seats")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jlockarray.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        MvcResult id3 = this.mockMvc
        .perform(
                get(
                        "/get-seats-recommendation?trainConnectionID=1&seatClass=2&numberOfPersons=1&arrival=10:30&departure=10:00&day=21-03-2016")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn();
        String sid3 = id3.getResponse().getContentAsString();
        jids = new JSONArray(sid3);
        jid = new JSONObject(jids.get(0).toString());
        sid3 = jid.getString("id");
        assertEquals(sid1, sid3);
        lockbody = "{\"seatID\": " + sid3 + ",\"trainConnectionID\": 1,\"departureTime\":\"21-03-2016 10:00\",\"arrivalTime\":\"21-03-2016 10:30\"}";
        jlockbody = new JSONObject(lockbody);
        jlockarray = new JSONArray();
        jlockarray.put(jlockbody);
        
        this.mockMvc
        .perform(
                post(
                        "/unlock-seats")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jlockarray.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        this.mockMvc
        .perform(
                get(
                        "/get-seats?trainConnectionID=1&seatClass=2&numberOfPersons=1&arrival=10:30&departure=10:00&day=21-03-2016")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        
        MvcResult result = this.mockMvc
        .perform(
                get(
                        "/get-seats-recommendation?trainConnectionID=1&seatClass=2&numberOfPersons=3&arrival=10:30&departure=10:00&day=21-03-2016")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn();
        String sids = result.getResponse().getContentAsString();
        jids = new JSONArray(sids);
        jid = new JSONObject(jids.get(0).toString());
        String sid4 = jid.getString("id");
        jid = new JSONObject(jids.get(1).toString());
        String sid5 = jid.getString("id");
        jid = new JSONObject(jids.get(2).toString());
        String sid6 = jid.getString("id");
        if(sid4.equals(sid5) || sid4.equals(sid6) || sid5.equals(sid6)){
            fail("same id error");
        }
        
        jlockarray = new JSONArray();
        lockbody = "{\"seatID\": " + sid4 + ",\"trainConnectionID\": 1,\"departureTime\":\"21-03-2016 10:00\",\"arrivalTime\":\"21-03-2016 10:30\"}";
        jlockbody = new JSONObject(lockbody);
        jlockarray.put(jlockbody);
        lockbody = "{\"seatID\": " + sid5 + ",\"trainConnectionID\": 1,\"departureTime\":\"21-03-2016 10:00\", \"arrivalTime\":\"21-03-2016 10:30\"}";
        jlockbody = new JSONObject(lockbody);
        jlockarray.put(jlockbody);
        
        this.mockMvc
        .perform(
                post(
                        "/lock-seats")
                        .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(jlockarray.toString().getBytes("utf-8")).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(status().is(409)).andExpect(content().contentType("application/json;charset=UTF-8"));
    }
}*/
