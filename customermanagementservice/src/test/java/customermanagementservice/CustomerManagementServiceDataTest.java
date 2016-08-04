package customermanagementservice;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.db.systel.bachelorproject2016.customermanagementservice.CustomerManagementService;
import com.db.systel.bachelorproject2016.customermanagementservice.api.CustomerManagementController;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.CustomerManagementDAO;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.JdbcCustomerManagementDAO;


public class CustomerManagementServiceDataTest {

    
      private String myJsonString1 =
      "{\"firstName\": \"Max\",\"lastName\": \"Mustermann\",\"address\": \"Musterstraße\",\"houseNumber\": 1,\"postCode\": 12345,\"city\": \"Musterstadt\",\"eMailAddress\":\"muster@mann.com\",\"paymentMethod\": \"Geld\"}"
      ; private MockMvc mockMvc; private JSONObject json1;
      
      
      @Mock 
      private CustomerManagementService customerManagementService;
      
      @InjectMocks 
      private CustomerManagementController customerManagementController;
      
      @BeforeClass 
      public static void connectionsetup() throws Exception { 
          DriverManagerDataSource src = new DriverManagerDataSource();

          src.setUsername("customer");
          src.setPassword("customer");
          src.setUrl("jdbc:mysql://" + "10.43.116.187" + ":" + 3308 + "/customermanagement?useSSL=false");
          src.setDriverClassName("com.mysql.jdbc.Driver");

          CustomerManagementService.customerManagementDAO = (CustomerManagementDAO) new JdbcCustomerManagementDAO();
          CustomerManagementService.customerManagementDAO.setDataSource(src); 
       }
      
      @Before public void setup() throws Exception { MockitoAnnotations.initMocks(this); this.mockMvc =
      MockMvcBuilders.standaloneSetup(customerManagementController).build(); json1 = new JSONObject(myJsonString1); }
      
      @Test 
      public void userCreating() throws Exception{ 
          MvcResult result = this.mockMvc .perform(
      post("/create-customer").contentType( MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      json1.toString().getBytes("utf-8"))).andExpect(status().isOk()).andReturn(); 
          String content = result.getResponse().getContentAsString();
      
      MvcResult result2 = this.mockMvc .perform( post("/create-customer").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      json1.toString().getBytes("utf-8"))).andExpect(status().isOk()).andReturn(); String content2 =
      result2.getResponse().getContentAsString();
      
      String accountJsonString = "{\"customerID\": " + content + ",\"username\": \"admin\",\"password\": \"admin\"}";
      JSONObject accountJson = new JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().isOk());
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(409));
      
      accountJsonString = "{\"customerID\": " + content2 + ",\"username\": \"admin\",\"password\": \"admin\"}";
      accountJson = new JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(409));
      
      accountJsonString = "{\"username\": \"admin\",\"password\": \"admin\"}"; accountJson = new
      JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(400));
      
      accountJsonString = "{\"customerID\": " + content + ",\"password\": \"admin\"}"; accountJson = new
      JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(400));
      
      accountJsonString = "{\"customerID\": " + content + ",\"username\": \"admin\"}"; accountJson = new
      JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(400));
      
      accountJsonString = "{\"customerID\": " + content + "}"; accountJson = new JSONObject(accountJsonString);
      
      this.mockMvc .perform( post("/create-account").contentType(
      MediaType.parseMediaType("application/json;charset=UTF-8")).content(
      accountJson.toString().getBytes("utf-8"))).andExpect(status().is(400));
      
      MvcResult getResult = this.mockMvc .perform(
      get("/login?username=admin&password=admin&getCustomerInformation=true").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")) .andReturn();
      String getContent = getResult.getResponse().getContentAsString(); JSONObject getJson = new
      JSONObject(getContent); assertEquals(("Max"), getJson.getString("firstName")); assertEquals(("Mustermann"),
      getJson.getString("lastName")); assertEquals(("Musterstraße"), getJson.getString("address")); assertEquals(("1"),
      getJson.getString("houseNumber")); assertEquals(("12345"), getJson.getString("postCode"));
      assertEquals(("Musterstadt"), getJson.getString("city")); assertEquals(("muster@mann.com"),
      getJson.getString("eMailAddress")); assertEquals(("Geld"), getJson.getString("paymentMethod"));
      
      this.mockMvc .perform( get("/login?username=admin&password=admin&getCustomerInformation=false").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
      
      this.mockMvc .perform( get("/login?username=admin&password=admin").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
      
      this.mockMvc .perform( get("/login?username=aladin&password=admin&getCustomerInformation=true").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().is(404)).andExpect(content().contentType("application/json;charset=UTF-8"));
      
      this.mockMvc .perform( get("/login?username=aladin&password=admin&getCustomerInformation=false").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().is(404)).andExpect(content().contentType("application/json;charset=UTF-8"));
      
      
      this.mockMvc .perform( get("/login?username=aladin&password=admin").accept(
      MediaType.parseMediaType("application/json;charset=UTF-8")))
      .andExpect(status().is(404)).andExpect(content().contentType("application/json;charset=UTF-8"));
      
      }
     

}
