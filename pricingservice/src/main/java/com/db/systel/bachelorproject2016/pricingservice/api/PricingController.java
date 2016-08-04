package com.db.systel.bachelorproject2016.pricingservice.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.db.systel.bachelorproject2016.pricingservice.domainmodel.EventStoreConnection;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.PriceCalculator;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.Seat;

@EnableAutoConfiguration
@Configuration
@Controller
public class PricingController {

    private static final Logger logger = LoggerFactory.getLogger(PricingController.class);

    @RequestMapping("/")
    public ResponseEntity<String> home() {

        return new ResponseEntity<String>("I am a new pricing service.", HttpStatus.OK);
    }

    /**
     * 
     * @param seatClass
     * @param seatArea
     * @param seatLocation
     * @param seatCompartmentType
     * @param upperLevel
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/get-price")
    public ResponseEntity<Float> getPrice(@RequestParam(value = "seatClass") int seatClass, @RequestParam(
            value = "seatArea", required = false, defaultValue = "default") String seatArea, @RequestParam(
            value = "seatLocation", required = false, defaultValue = "default") String seatLocation, @RequestParam(
            value = "seatCompartmentType", required = false, defaultValue = "default") String seatCompartmentType,
            @RequestParam(value = "upperLevel", required = false, defaultValue = "false") boolean upperLevel) {

        //System.out.println("Received update to calculate price");
        logger.info("Received update to calculate price");
        float price = PriceCalculator.calculatePrice(new Seat(seatClass, seatArea, seatLocation, seatCompartmentType,
                upperLevel));

        return new ResponseEntity<Float>(price, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-dynamic-price")
    public ResponseEntity<String> getDynamicPrice(@RequestParam(value = "seatClass") Integer seatClass,
            @RequestParam(value = "departureTime") String departureTime,
            @RequestParam(value = "arrivalTime") String arrivalTime,
            @RequestParam(value = "trainConnectionID") Integer trainConnectionID) {

        System.out.println("Received update to calculate price");

        String price = PriceCalculator
                .calculateDynamicPrice(seatClass, departureTime, arrivalTime, trainConnectionID);

        price = price.replace(",", ".");

        return new ResponseEntity<String>(price, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/increase-allocation")
    public ResponseEntity<String> increaseAllocation(@RequestParam(value = "departureTime") String departureTime,
            @RequestParam(value = "arrivalTime") String arrivalTime,
            @RequestParam(value = "trainConnectionID") Integer trainConnectionID) {

        EventStoreConnection.decreaseNumberOfSeats(departureTime, arrivalTime, trainConnectionID);

        String result = EventStoreConnection.getNumberOfSeats(departureTime, arrivalTime, trainConnectionID);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/decrease-allocation")
    public ResponseEntity<String> decreaseAllocation(@RequestParam(value = "departureTime") String departureTime,
            @RequestParam(value = "arrivalTime") String arrivalTime,
            @RequestParam(value = "trainConnectionID") Integer trainConnectionID) {

        EventStoreConnection.increaseNumberOfSeats(departureTime, arrivalTime, trainConnectionID);

        String result = EventStoreConnection.getNumberOfSeats(departureTime, arrivalTime, trainConnectionID);

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }
}
