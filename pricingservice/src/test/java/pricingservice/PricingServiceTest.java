package pricingservice;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.db.systel.bachelorproject2016.pricingservice.domainmodel.PriceCalculator;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.Seat;

public class PricingServiceTest {

	@Test
	public void testFirstClass() {
		Seat seat = new Seat(1, "area", "location", "compartment", false);
		assertEquals(PriceCalculator.calculatePrice(seat), 0.0f, 0.001);
	}

	@Test
	public void testSecondClass() {
		Seat seat = new Seat(2, "area", "location", "compartment", false);
		assertEquals(PriceCalculator.calculatePrice(seat), 4.5f, 0.001);
	}

}
