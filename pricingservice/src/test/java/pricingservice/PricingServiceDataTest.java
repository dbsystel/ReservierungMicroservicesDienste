/*package pricingservice;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

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

import com.db.systel.bachelorproject2016.pricingservice.PricingService;
import com.db.systel.bachelorproject2016.pricingservice.RabbitConnector;
import com.db.systel.bachelorproject2016.pricingservice.api.PricingController;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.AllocationDecreasedQueueListener;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.AllocationIncreasedQueueListener;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.ShutdownConnectionListener;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisURI;

public class PricingServiceDataTest {
    private MockMvc mockMvc;

    public static RedisClient redisClient;

    @Mock
    private PricingService pricingtService;

    @InjectMocks
    private PricingController pricingController;

    @BeforeClass
    public static void connectionsetup() throws Exception {
        PricingService.dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        PricingService.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        PricingService.redisClient = new RedisClient(RedisURI.create("redis://" + "10.43.116.187" + ":" + 6379));
        redisClient = new RedisClient(RedisURI.create("redis://" + "10.43.116.187" + ":" + 6379));
        PricingService.connectedToRedis = true;

        //Setzt Host, Port etc. der Factory
        System.out.println("Conntecting to " + "10.43.116.187" + ":" + 5672);
        PricingService.rabbitConnFactory = new CachingConnectionFactory();
        PricingService.rabbitConnFactory.setHost("10.43.116.187");
        PricingService.rabbitConnFactory.setPort(5672);
        PricingService.rabbitConnFactory.setVirtualHost("host_db");
        PricingService.rabbitConnFactory.setUsername("pricing");
        PricingService.rabbitConnFactory.setPassword("pricing");
        PricingService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

        //Deklariert Admin und Template auf Grundlage der Factory
        PricingService.admin = new RabbitAdmin(PricingService.rabbitConnFactory);
        PricingService.template = new RabbitTemplate(PricingService.rabbitConnFactory);

        //Initalisiert listeneder, damit auf die MessageQueues gehört werden kann
        RabbitConnector.initListener("pricing.bookings_confirmed", "bookings_confirmed", "confirm",
                new AllocationIncreasedQueueListener());
        RabbitConnector.initListener("pricing.bookings_cancelled", "bookings_cancelled", "delete",
                new AllocationDecreasedQueueListener());

        PricingService.connectedToRabbit = true;

        RedisConnection<String, String> connection;
        connection = PricingService.redisClient.connect();
        Long time = PricingService.dateTimeFormat.parse("26-04-2016 10:00" + ":00").getTime();
        connection.set("1:" + time, "10:10");
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(pricingController).build();
    }

    @Test
    public void staticPriceTests() throws Exception {
        MvcResult getResult = this.mockMvc
                .perform(
                        get("/get-price?seatClass=1").accept(
                                MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        String getContent = getResult.getResponse().getContentAsString();
        assertEquals("0.0", getContent);

        MvcResult getResult2 = this.mockMvc
                .perform(
                        get("/get-price?seatClass=2").accept(
                                MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        getContent = getResult2.getResponse().getContentAsString();
        assertEquals("4.5", getContent);
    }

    @Test
    public void dynamicPriceTests() throws Exception {
        MvcResult getResult = this.mockMvc
                .perform(
                        get("/get-dynamic-price?seatClass=2&departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1"))
                .andExpect(status().isOk()).andReturn();
        String getContent = getResult.getResponse().getContentAsString();
        double result = Double.parseDouble(getContent);
        assertEquals(5.0, result, 0.001);

        MvcResult postResult = this.mockMvc
                .perform(
                        post(
                                "/increase-allocation?departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1")
                                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        String postContent = postResult.getResponse().getContentAsString();
        assertEquals("9:10", postContent);

        MvcResult postResult2 = this.mockMvc
                .perform(
                        post(
                                "/increase-allocation?departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1")
                                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        postContent = postResult2.getResponse().getContentAsString();
        assertEquals("8:10", postContent);

        MvcResult getResult2 = this.mockMvc
                .perform(
                        get(
                                "/get-dynamic-price?seatClass=2&departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1")
                                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        getContent = getResult2.getResponse().getContentAsString();
        result = Double.parseDouble(getContent);
        assertEquals(6.25, result, 0.001);

        MvcResult postResult3 = this.mockMvc
                .perform(
                        post(
                                "/decrease-allocation?departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1")
                                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        postContent = postResult3.getResponse().getContentAsString();
        assertEquals("9:10", postContent);

        MvcResult getResult3 = this.mockMvc
                .perform(
                        get(
                                "/get-dynamic-price?seatClass=2&departureTime=26-04-2016 10:00&arrivalTime=26-04-2016 10:30&trainConnectionID=1")
                                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn();
        getContent = getResult3.getResponse().getContentAsString();
        result = Double.parseDouble(getContent);
        assertEquals(5.56, result, 0.001);
    }

}*/
