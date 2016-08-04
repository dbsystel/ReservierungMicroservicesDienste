package com.db.systel.bachelorproject2016.bookingservice.api;

import java.util.ArrayList;
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

import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.Booking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.CustomerBooking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.PartialBooking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.CassandraConnection;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.EventStoreConnection;

@Configuration
@EnableAutoConfiguration
@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private static boolean checkCassandraConnection() {

        if (!BookingService.connectedToCassandra) {
            logger.info("Currently not connected to Cassandra");
            System.out.println("Currently not connected to Cassandra");
            return false;
        }

        return true;
    }

    private static boolean checkRedisConnection() {

        if (!BookingService.connectedToRedis) {
            logger.info("Currently not connected to Redis");
            System.out.println("Currently not connected to Redis");
            return false;
        }

        return true;
    }

    //Momentan nicht genutzt 
    private static boolean checkRabbitConnection() {

        if (!BookingService.connectedToRabbit) {
            logger.info("Currently not connected to Rabbit");
            System.out.println("Currently not connected to Rabbit");
            return false;
        }

        return true;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> home() {
        return new ResponseEntity<String>("I am a BookingService.", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-customer-bookings")
    public ResponseEntity<?> getCustomerBookings(@RequestParam int customerID) {
        System.out.println("Received request for bookings of customer " + customerID);
        logger.info("Received request for bookings of customer " + customerID);

        if (checkCassandraConnection()) {
            List<Booking> bookings = CassandraConnection.getBookingsForCustomer(customerID);
            return new ResponseEntity<List<Booking>>(bookings, HttpStatus.OK);
        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/initiate-partial-bookings")
    public ResponseEntity<?> partialBookingResults(@RequestBody List<PartialBooking> partialBookings) {
        System.out.println(partialBookings.toString());

        System.out.print("Trying to write Guards into the event store");
        logger.info("Trying to write Guards into the event store");

        if (BookingService.connectedToRedis || checkCassandraConnection()) {

            List<PartialBooking> failedBookings = new ArrayList<PartialBooking>();

            for (PartialBooking pb : partialBookings) {

                System.out.println("Current: " + pb.toString());
                // Negierung --> insertGuard funktioniert -> gibt true zurück ->
                // nichts schreiben
                if (CassandraConnection.findCollidingBooking(pb.getTrainConnectionID(), pb.getSeatID(),
                        pb.getDepartureTime(), pb.getArrivalTime())
                        || !EventStoreConnection.insertGuard(pb)) {

                    failedBookings.add(pb);
                    //System.out.println(String.format("Could not write guard for %s from %s to %s for connection %s",
                    //        pb.getSeatID(), pb.getStart(), pb.getDestination(), pb.getTrainConnectionID()));
                    logger.info(String.format("Could not write guard for %s from %s to %s for connection %s",
                            pb.getSeatID(), pb.getStart(), pb.getDestination(), pb.getTrainConnectionID()));

                } else {
                    BookingService.queueFeeder.bookingInitiate(pb);
                }
            }

            if (failedBookings.size() == 0) {
                //System.out.println("Success");
                logger.info("Success");
                return new ResponseEntity<List<PartialBooking>>(partialBookings, HttpStatus.OK);
            }
            return new ResponseEntity<List<PartialBooking>>(failedBookings, HttpStatus.CONFLICT);

        }
        return new ResponseEntity<String>("Not connected to Cassandra or Redis", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/confirm-partial-bookings")
    public ResponseEntity<?> confirmBookings(@RequestParam int customerID, @RequestParam String paymentMethod,
            @RequestBody List<PartialBooking> partialBookings) {

        System.out.println("Customer with ID " + customerID + " confirmed " + partialBookings.size()
                + " partial Bookings");
        logger.info("Customer with ID " + customerID + " confirmed " + partialBookings.size() + " partial Bookings");

        if (checkCassandraConnection()) {
            Booking booking = CassandraConnection.insertBooking(customerID, paymentMethod, partialBookings);

            for (PartialBooking pb : partialBookings) {
                BookingService.queueFeeder.bookingConfirm(pb);
            }

            return new ResponseEntity<Booking>(booking, HttpStatus.OK);
        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/cancel-partial-bookings")
    public ResponseEntity<String> cancelBookings(@RequestBody List<PartialBooking> partialBookings) {

        if (checkRedisConnection()) {

            for (PartialBooking pb : partialBookings) {
                try {
                    EventStoreConnection.removeGuard(pb);
                    BookingService.queueFeeder.bookingCancel(pb);

                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    logger.error(e.getMessage());
                    return new ResponseEntity<String>("Could not delete partial bookings", HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<String>("Deleted partial bookings", HttpStatus.OK);
        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/change-partial-booking")
    public ResponseEntity<?> changeBookings(@RequestParam String partialBookingID,
            @RequestBody PartialBooking newInformation) {

        System.out.println("Updating a partial Booking");
        logger.info("Updating a partial Booking");

        if (checkCassandraConnection()) {

            try {
                PartialBooking origin = CassandraConnection.changePartialBooking(partialBookingID, newInformation);

                BookingService.queueFeeder.bookingChange(origin, newInformation);

                return new ResponseEntity<PartialBooking>(origin, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                logger.info(e.getMessage());
                return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete-partial-booking")
    public ResponseEntity<String> deleteBookings(@RequestParam String partialBookingID) {

        System.out.println("Deleting a partial Booking");
        logger.info("Deleting a partial Booking");

        if (checkCassandraConnection()) {

            try {
                PartialBooking partialBooking = CassandraConnection.deletePartialBooking(partialBookingID);

                if (partialBooking != null) {

                    BookingService.queueFeeder.bookingDelete(partialBooking);
                    return new ResponseEntity<String>("Successfully deleted", HttpStatus.OK);
                }

                return new ResponseEntity<String>("Could not delete", HttpStatus.BAD_REQUEST);

            } catch (Exception e) {
                //System.out.println(e.getMessage());
                logger.error(e.getMessage());
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/disable-partial-bookings")
    public ResponseEntity<?> disableBookings(@RequestBody List<Integer> seatIDs) {

        if (checkCassandraConnection()) {

            List<CustomerBooking> customerBookings = CassandraConnection.updateDisabledBookings(seatIDs);
            return new ResponseEntity<List<CustomerBooking>>(customerBookings, HttpStatus.OK);

        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/cancel-train-connection")
    public ResponseEntity<?> cancelTrainConnection(@RequestParam int trainConnectionID, String day) {

        if (checkCassandraConnection()) {

            List<CustomerBooking> customerBookings = CassandraConnection.updateCancelledTrainConnection(
                    trainConnectionID, day);
            return new ResponseEntity<List<CustomerBooking>>(customerBookings, HttpStatus.OK);

        }

        return new ResponseEntity<String>("Not connected to Cassandra", HttpStatus.INTERNAL_SERVER_ERROR);

    }
}