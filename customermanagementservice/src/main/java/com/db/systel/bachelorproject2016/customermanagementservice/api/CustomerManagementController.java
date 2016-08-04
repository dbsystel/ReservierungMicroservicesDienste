package com.db.systel.bachelorproject2016.customermanagementservice.api;

import java.util.List;

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

import com.db.systel.bachelorproject2016.customermanagementservice.CustomerManagementService;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.Account;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.Customer;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.CustomerContact;

/*
 * RequestMapping der einzelnen Pfade
 * 
 * Leitet die Eingangsparameter and die Datenbankschnittstelle weiter
 * --> Jedes Mapping ist einfach ein Aufruf der entsprechenden Datenbankfunktion über die customerManagementDAO
 */
@EnableAutoConfiguration
@Controller
@Configuration
public class CustomerManagementController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerManagementController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> home() {
        return new ResponseEntity<String>("I am a CustomerManagementService.", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public ResponseEntity<?> customerBookings(@RequestParam(value = "username") String username, @RequestParam(
            value = "password") String password, @RequestParam(value = "getCustomerInformation", required = false,
            defaultValue = "false") boolean getCustomerInformation) {

        logger.info("Received login request");
        Customer customer = CustomerManagementService.customerManagementDAO.getCustomer(username, password,
                getCustomerInformation);

        if (customer != null) {
            logger.info("Could find customer.");
            return new ResponseEntity<Customer>(customer, HttpStatus.OK);
        } else {
            logger.info("Couldn't find customer. Username or password may have been incorrect");
            return new ResponseEntity<String>("Incorrect username or password", HttpStatus.NOT_FOUND);
        }

    }

    @RequestMapping(method = RequestMethod.POST, value = "/create-customer", consumes = "application/json")
    public ResponseEntity<Integer> customerBookings(@RequestBody Customer customer) {

        logger.info("Received request to insert new customer");

        Integer customerID = CustomerManagementService.customerManagementDAO.insertCustomer(customer);

        return new ResponseEntity<Integer>(customerID, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/create-account", consumes = "application/json")
    public ResponseEntity<String> customerBookings(@RequestBody Account account) {

        logger.info("Received request to insert new account");

        if (account.getPassword() == null || account.getPassword() == "" || account.getUsername() == null
                || account.getUsername() == null) {
            logger.info("Request was missing username or password");
            return new ResponseEntity<String>("No username or password provided", HttpStatus.BAD_REQUEST);
        }

        if (account.getCustomerID() == null) {
            logger.info("Request was missing a customer ID");
            return new ResponseEntity<String>("No customerID provided", HttpStatus.BAD_REQUEST);
        }

        String creation = CustomerManagementService.customerManagementDAO.insertAccount(account);

        if (creation.equals("Account successfully created")) {
            logger.info("Successfully created account");
            return new ResponseEntity<String>(creation, HttpStatus.OK);
        }
        logger.info("Failed: " + creation);
        return new ResponseEntity<String>(creation, HttpStatus.CONFLICT);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete-account")
    public ResponseEntity<String> accountDeletion(@RequestParam int customerID) {

        logger.info("Received request to delete account");

        if (CustomerManagementService.customerManagementDAO.deleteAccount(customerID)) {
            return new ResponseEntity<String>("Account successfully deleted", HttpStatus.OK);
        }
        return new ResponseEntity<String>("Could not delete account", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete-customer")
    public ResponseEntity<String> customerDeletion(@RequestParam int customerID) {

        logger.info("Received request to delete customer");

        if (CustomerManagementService.customerManagementDAO.deleteCustomer(customerID)) {
            return new ResponseEntity<String>("Customer successfully deleted", HttpStatus.OK);
        }
        return new ResponseEntity<String>("Could not delete customer", HttpStatus.NOT_FOUND);
    }

    //Wird nicht mehr benötigt
    @RequestMapping(method = RequestMethod.GET, value = "/get-contact-information")
    public ResponseEntity<List<CustomerContact>> customerBookings(@RequestParam List<Integer> customerIDs) {

        logger.info("Received request for contact information for the following ids " + customerIDs);
        List<CustomerContact> customerContacts = CustomerManagementService.customerManagementDAO
                .getContactInformation(customerIDs);
        return new ResponseEntity<List<CustomerContact>>(customerContacts, HttpStatus.OK);
    }

}
