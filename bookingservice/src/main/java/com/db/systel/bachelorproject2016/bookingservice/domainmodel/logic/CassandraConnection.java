package com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.db.systel.bachelorproject2016.bookingservice.BookingService;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.Booking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.CustomerBooking;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.data.PartialBooking;

public class CassandraConnection {

    private static final Logger logger = LoggerFactory.getLogger(CassandraConnection.class);

    public static Booking insertBooking(int customerID, String paymentMethod, List<PartialBooking> partialBookings) {

        String bookingUUID = UUID.randomUUID().toString();

        PreparedStatement insertBooking = BookingService.session.prepare("INSERT INTO booking "
                + "(id, customer_id, payment_method) VALUES (?, ?, ?)");

        PreparedStatement insertPartialBooking = BookingService.session
                .prepare("INSERT INTO partial_booking "
                        + "(id, booking_id, seat_id, train_connection_id, start, destination, departure_time, arrival_time, price, state)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'valid')");

        try {

            BookingService.session.execute(insertBooking.bind(bookingUUID, customerID, paymentMethod));

            // Add each partial booking
            for (PartialBooking pb : partialBookings) {

                /*
                 * Die Daten kommen für Menschen gut lesbar rein und müssen für die Datenbank in Longs umgewandelt
                 * werden
                 */
                Date departure = BookingService.dateFormat.parse(pb.getDepartureTime());
                Date arrival = BookingService.dateFormat.parse(pb.getArrivalTime());

                String partialBookingUUID = UUID.randomUUID().toString();
                BookingService.session.execute(insertPartialBooking.bind(partialBookingUUID, bookingUUID,
                        pb.getSeatID(), pb.getTrainConnectionID(), pb.getStart(), pb.getDestination(),
                        departure.getTime(), arrival.getTime(), pb.getPrice()));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);

        }
        //TODO
        return getBooking(bookingUUID);
    }

    public static PartialBooking changePartialBooking(String partialBookingID, PartialBooking partialBooking) {

        PartialBooking oldData = null;
        PreparedStatement select = BookingService.session.prepare("SELECT * FROM partial_booking WHERE id = ?");

        String cql = "UPDATE partial_booking SET ";

        try {
            if (partialBooking.getSeatID() != null) {
                cql += " seat_id =" + partialBooking.getSeatID() + ", ";
            }
            if (partialBooking.getTrainConnectionID() != null) {
                cql += " train_connection_id =" + partialBooking.getTrainConnectionID() + ", ";
            }
            if (partialBooking.getStart() != null) {
                cql += " start = '" + partialBooking.getStart() + "', ";
            }
            if (partialBooking.getDestination() != null) {
                cql += " destination = '" + partialBooking.getDestination() + "', ";
            }
            if (partialBooking.getDepartureTime() != null) {
                cql += " departure_time ="
                        + BookingService.dateFormat.parse(partialBooking.getDepartureTime()).getTime() + ", ";
            }
            if (partialBooking.getArrivalTime() != null) {
                cql += " arrival_time =" + BookingService.dateFormat.parse(partialBooking.getArrivalTime()).getTime()
                        + ", ";
            }
            if (partialBooking.getPrice() != null) {
                cql += " price =" + partialBooking.getPrice() + ", ";
            }

            cql = cql.substring(0, cql.length() - 2);
            cql += " WHERE id = ?;";

            PreparedStatement updated = BookingService.session.prepare(cql);

            ResultSet partialBookingRS = BookingService.session.execute(select.bind(partialBookingID));

            BookingService.session.execute(updated.bind(partialBookingID));
            for (Row row : partialBookingRS) {
                oldData = parsePartialBooking(row);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return oldData;

    }

    public static PartialBooking deletePartialBooking(String partialBookingID) {

        PartialBooking partialBooking = null;

        PreparedStatement select = BookingService.session.prepare("SELECT * FROM partial_booking WHERE id = ? ");
        PreparedStatement delete = BookingService.session.prepare("DELETE FROM partial_booking WHERE id = ? ");

        try {
            ResultSet partialBookingRS = BookingService.session.execute(select.bind(partialBookingID));

            for (Row row : partialBookingRS) {
                partialBooking = parsePartialBooking(row);
            }

            BookingService.session.execute(delete.bind(partialBookingID));

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return partialBooking;

    }

    public static Booking getBooking(String bookingID) {

        Booking booking = null;

        PreparedStatement selectBookings = BookingService.session.prepare("SELECT * FROM booking WHERE id = ?");
        PreparedStatement selectPartialBookings = BookingService.session
                .prepare("SELECT * FROM partial_booking WHERE booking_id = ? ALLOW FILTERING");

        try {
            ResultSet bookingsRS = BookingService.session.execute(selectBookings.bind(bookingID));

            for (Row row : bookingsRS) {
                List<PartialBooking> partialBookings = new ArrayList<PartialBooking>();
                ResultSet partialBookingRS = BookingService.session.execute(selectPartialBookings.bind(bookingID));
                for (Row partialBookingsRow : partialBookingRS) {
                    partialBookings.add(parsePartialBooking(partialBookingsRow));
                }
                booking = parseBooking(row, partialBookings);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return booking;

    }

    public static List<Booking> getBookingsForCustomer(int customerID) {

        List<Booking> bookings = new ArrayList<Booking>();

        PreparedStatement selectBookings = BookingService.session
                .prepare("SELECT * FROM booking WHERE customer_id = ?");
        PreparedStatement selectPartialBookings = BookingService.session
                .prepare("SELECT * FROM partial_booking WHERE booking_id = ?");

        try {
            ResultSet bookingsRS = BookingService.session.execute(selectBookings.bind(customerID));

            // Erst die Buchungen auslesen, die zum Kunden gehören

            for (Row row : bookingsRS) {
                List<PartialBooking> partialBookings = new ArrayList<PartialBooking>();
                ResultSet partialBookingsRS = BookingService.session.execute(selectPartialBookings.bind(row
                        .getString("id")));

                for (Row partialBookingRow : partialBookingsRS) {
                    partialBookings.add(parsePartialBooking(partialBookingRow));
                }
                bookings.add(parseBooking(row, partialBookings));
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return bookings;
    }

    // TODO: Diese Methode könnte man eventuell splitten
    public static List<CustomerBooking> updateDisabledBookings(List<Integer> seatIDs) {

        List<CustomerBooking> customerBookings = new ArrayList<CustomerBooking>();

        // Für die Gruppierung der Teilbuchungen und Kunden
        Map<Integer, List<PartialBooking>> dict = new HashMap<Integer, List<PartialBooking>>();

        try {
            // Updated zuerst alle betroffenen Teilbuchungen
            for (int seatID : seatIDs) {
                PreparedStatement selectBookings = BookingService.session
                        .prepare("SELECT * FROM partial_booking WHERE seat_id = ? ALLOW FILTERING");
                ResultSet cancelledBooking = BookingService.session.execute(selectBookings.bind(seatID));

                for (Row r : cancelledBooking) {

                    PreparedStatement invalidateBookings = BookingService.session
                            .prepare("UPDATE partial_booking SET state = 'invalid' WHERE id = ?");

                    BookingService.session.execute(invalidateBookings.bind(r.getString("id")));
                }

            }

            // alle Buchungen wählen

            //Kann nicht mit der Schleife oben zusammen gelegt werden, weil man die Daten nach dem Update nochmal ziehen muss 
            List<Row> partialBookingsRS = new ArrayList<Row>();

            for (int seatID : seatIDs) {
                PreparedStatement selectBookings = BookingService.session
                        .prepare("SELECT * FROM partial_booking WHERE seat_id = ? ALLOW FILTERING");
                ResultSet tmp = BookingService.session.execute(selectBookings.bind(seatID));

                for (Row r : tmp) {
                    partialBookingsRS.add(r);
                }

            }

            // PartialBooking-Objekte erstellen
            for (Row row : partialBookingsRS) {

                String bookingID = row.getString("booking_id");

                PreparedStatement selectBookings = BookingService.session
                        .prepare("SELECT * FROM booking WHERE id = ? ALLOW FILTERING");

                ResultSet tmp = BookingService.session.execute(selectBookings.bind(bookingID));

                for (Row row2 : tmp) {
                    int customerID = row2.getInt("customer_id");

                    PartialBooking pb = parsePartialBooking(row);

                    // Wenn der Kunde schon existiert, wird ihm die neue Teilbuchung
                    // zugewiesen
                    if (dict.containsKey(customerID)) {
                        List<PartialBooking> currentBookings = dict.get(customerID);
                        currentBookings.add(pb);
                        dict.replace(customerID, currentBookings);
                    }
                    // Ansonsten wird ein neuer Eintrag im Dictionary angelegt
                    else {
                        List<PartialBooking> currentBookings = new ArrayList<PartialBooking>();
                        currentBookings.add(pb);
                        dict.put(customerID, currentBookings);
                    }
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            //System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        // Dictionary in ArrayList tranferieren
        for (int key : dict.keySet()) {
            customerBookings.add(new CustomerBooking(key, dict.get(key)));
        }
        return customerBookings;
    }

    public static boolean findCollidingBooking(int trainConnectionID, int seatID, String departureTime,
            String arrivalTime) {

        BookingService.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {

            /*
             * CQL bietet keine Logik für Oder-Statements, also müssen wir die SQL-Anfrage in 3 Teile splitten
             */

            String[] condition = { "departure_time >= ? AND departure_time < ?",
                    "arrival_time > ? AND arrival_time <= ?", "departure_time < ? AND arrival_time > ?" };
            String findCollidingBooking = "SELECT * FROM partial_booking WHERE train_connection_id = ? AND seat_id = ? AND ";

            PreparedStatement findBooking = null;
            ResultSet rs = null;

            for (int i = 0; i < condition.length; i++) {
                findBooking = BookingService.session
                        .prepare(findCollidingBooking + condition[i] + " ALLOW FILTERING");
                rs = BookingService.session.execute(findBooking.bind(trainConnectionID, seatID,
                        BookingService.dateFormat.parse(departureTime).getTime(),
                        BookingService.dateFormat.parse(arrivalTime).getTime()));

                for (Row row : rs) {
                    System.out.println("Found colliding booking on");
                    return true;
                }
            }

            System.out.println("Didn't find colliding booking");
            return false;

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<CustomerBooking> updateCancelledTrainConnection(int trainConnectionID, String day) {
        List<CustomerBooking> customerBookings = new ArrayList<CustomerBooking>();

        System.out.println(trainConnectionID + "\t" + day);

        try {

            String selectPartialBookings = "SELECT id, booking_id FROM partial_booking WHERE train_connection_id = ? AND departure_time > ? AND departure_time < ? ALLOW FILTERING;";

            ArrayList<String> partialBookingIDs = new ArrayList<String>();
            Set<String> bookingIDs = new HashSet<String>();
            String updatePartialBookings = "UPDATE partial_booking SET state = 'invalid' WHERE id = ?;";

            PreparedStatement selectBookings = BookingService.session.prepare(selectPartialBookings);

            ResultSet partialBookingsRS = BookingService.session.execute(selectBookings.bind(trainConnectionID,
                    BookingService.dateFormat.parse(day + " 00:00").getTime(),
                    BookingService.dateFormat.parse(day + " 00:00").getTime() + 86400000));

            //IDs der betroffenen Teilbuchungen auslesen und deren Hauptbuchungen
            for (Row row : partialBookingsRS) {
                partialBookingIDs.add(row.getString("id"));
                bookingIDs.add(row.getString("booking_id"));
            }

            //Betroffene Teilbuchungen durch ID invalidieren 
            PreparedStatement updateBookings = BookingService.session.prepare(updatePartialBookings);
            for (String id : partialBookingIDs) {
                BookingService.session.execute(updateBookings.bind(id));

            }

            PreparedStatement selectBookingInformation = BookingService.session
                    .prepare("SELECT * FROM booking WHERE id = ? ALLOW FILTERING;");

            PreparedStatement selectPBtoBooking = BookingService.session
                    .prepare("SELECT * FROM partial_booking WHERE booking_id = ? ALLOW FILTERING;");

            //Alle Informationen der Haupt- und Teilbuchungen auslesen und zusammensetzen
            for (String id : bookingIDs) {

                //HauptBuchung aulesen
                ResultSet bookingInformation = BookingService.session.execute(selectBookingInformation.bind(id));

                //Teilbuchungen auslesen 
                for (Row row : bookingInformation) {

                    List<PartialBooking> pbs = new ArrayList<PartialBooking>();
                    ResultSet partialBookings = BookingService.session.execute(selectPBtoBooking.bind(id));
                    for (Row row2 : partialBookings) {
                        pbs.add(parsePartialBooking(row2));
                    }

                    CustomerBooking cb = new CustomerBooking(row.getInt("customer_id"), pbs);
                    customerBookings.add(cb);
                }

            }

        } catch (Exception e) {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        return customerBookings;
    }

    private static PartialBooking parsePartialBooking(Row partialBookingRow) {

        PartialBooking partialBooking = null;
        try {
            Date departure = new Date(partialBookingRow.getLong("departure_time"));
            Date arrival = new Date(partialBookingRow.getLong("arrival_time"));

            partialBooking = new PartialBooking(partialBookingRow.getString("id"),
                    partialBookingRow.getInt("seat_id"), partialBookingRow.getInt("train_connection_id"),
                    partialBookingRow.getString("start"), partialBookingRow.getString("destination"),
                    BookingService.dateFormat.format(departure), BookingService.dateFormat.format(arrival),
                    partialBookingRow.getDouble("price"), partialBookingRow.getString("state"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

        return partialBooking;

    }

    private static Booking parseBooking(Row bookingRow, List<PartialBooking> partialBookings) {

        Booking booking = null;
        try {
            booking = new Booking(bookingRow.getString("id"), bookingRow.getString("payment_method"), partialBookings);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

        return booking;

    }

}
