package com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.systel.bachelorproject2016.seatoverviewservice.SeatOverviewService;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.IntermediateStation;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.Seat;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.SeatAllocation;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.data.TrainConnection;

public class JdbcSeatOverviewDAO implements SeatOverviewDAO {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSeatOverviewDAO.class);

    private DataSource dataSource;

    private ClassLoader classLoader = getClass().getClassLoader();

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<TrainConnection> getTrainConnections(String start, String destination, Long departureTime,
            Integer day, boolean departure, Long timeTillDay) {

        List<TrainConnection> trainConnections = new ArrayList<TrainConnection>();
        Connection conn = null;

        /*
         * Format des Datums für die Rückgabe an den Klientne
         */

        try {
            String getTrainConnections = null;
            /*
             * Je nachdem, ob departure wahr oder falsch ist, beziehen wir uns auf die Ankunfts- oder Abfahrtszeit und
             * laden andere Queries. Das hier ist die Queries um alle passenden Zugfahrten zu wählen
             * 
             * TODO: Die Queries da zusammenfasse, wo es geht
             */
            if (departure) {
                /*
                 * Die Queries liegen in den Resourcen und werden hier ausgelesen.
                 */
                getTrainConnections = IOUtils.toString(
                        classLoader.getResourceAsStream("Queries/get-train-connections-departure"), "UTF-8");
            } else {
                getTrainConnections = IOUtils.toString(
                        classLoader.getResourceAsStream("Queries/get-train-connections-arrival"), "UTF-8");
            }
            /*
             * Query für die Zusatzinformationen (Zugnummer, Kategorie)
             */
            String getTrainInformation = IOUtils.toString(
                    classLoader.getResourceAsStream("Queries/get-additional-information"), "UTF-8");

            /*
             * Query für die Zwischenhalte
             */
            String getIntermediateStations = IOUtils.toString(
                    classLoader.getResourceAsStream("Queries/get-intermediate-stations"), "UTF-8");

            conn = dataSource.getConnection();
            PreparedStatement getTrainConnectionsStmt = conn.prepareStatement(getTrainConnections);

            PreparedStatement getTrainInformationStmt = conn.prepareStatement(getTrainInformation);

            PreparedStatement getIntermediateStationsStmt = conn.prepareStatement(getIntermediateStations);

            /*
             * Das Statement braucht die Parameter. Die 120 beziehen sich darauf, dass alle passenden Fahrten in einem
             * Zeitabschnitt von 2 Stunden nach der Wunschzeit angezeigt werden
             */
            getTrainConnectionsStmt.setString(1, start);
            getTrainConnectionsStmt.setInt(2, day);
            getTrainConnectionsStmt.setString(3, destination);
            getTrainConnectionsStmt.setInt(4, day);
            getTrainConnectionsStmt.setLong(5, departureTime);
            getTrainConnectionsStmt.setLong(6, departureTime + 7200000);

            logger.info("Executing Query: " + getTrainConnections);

            ResultSet trainConnectionRS = getTrainConnectionsStmt.executeQuery();
            /*
             * Jeder Zugfahrt hat nun eine eigene Zeile im Resultset
             */
            while (trainConnectionRS.next()) {

                /*
                 * Für jede dieser Zugfahrten müssen noch die Zusatzinformationen gesammelt werden
                 */
                logger.info("Executing Query: " + getTrainInformation + " and " + getIntermediateStations + "for id "
                        + trainConnectionRS.getInt("id"));

                getTrainInformationStmt.setInt(1, trainConnectionRS.getInt("id"));
                getIntermediateStationsStmt.setInt(1, trainConnectionRS.getInt("id"));
                getIntermediateStationsStmt.setLong(2, trainConnectionRS.getLong("arrival_without_offset"));
                getIntermediateStationsStmt.setLong(3, trainConnectionRS.getLong("departure_without_offset"));

                ResultSet trainInformationRS = getTrainInformationStmt.executeQuery();
                ResultSet intermediateStationsRS = getIntermediateStationsStmt.executeQuery();

                /*
                 * next ist immer nötig, damit man in das ResultSet rein kommt (also in die erste Zeile)
                 */
                trainInformationRS.next();

                List<IntermediateStation> intermediateStations = new ArrayList<IntermediateStation>();
                /*
                 * Jede Zwischenstation muss zu einem Datenobjekt werden
                 */
                while (intermediateStationsRS.next()) {

                    Date stopTime = new Date(timeTillDay + intermediateStationsRS.getLong("time_of_stop")
                            + trainConnectionRS.getLong("departure_time"));

                    intermediateStations.add(new IntermediateStation(
                            intermediateStationsRS.getString("station_name"), SeatOverviewService.dateTimeFormat
                                    .format(stopTime)));
                }

                /*
                 * Auf Grundlage dessen werden dann die einzelnen Ergebnisse zusammengesetzt und ganz am Ende zurück
                 * gegeben
                 */

                Date departureDate = new Date(timeTillDay + trainConnectionRS.getLong("departure"));
                Date arrivalDate = new Date(timeTillDay + trainConnectionRS.getLong("arrival"));

                trainConnections.add(new TrainConnection(trainConnectionRS.getInt("id"), trainInformationRS
                        .getInt("number"), trainInformationRS.getString("category"),
                        SeatOverviewService.dateTimeFormat.format(departureDate), SeatOverviewService.dateTimeFormat
                                .format(arrivalDate), intermediateStations));

            }
            getTrainConnectionsStmt.close();
            getIntermediateStationsStmt.close();
            getTrainInformationStmt.close();

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                }
            }

        }
        return trainConnections;
    }

    @Override
    public List<Seat> getSeat(Integer trainConnectionID, Integer seatClass, String area, String location,
            String compartmentType, Boolean upperLevel, Integer numberOfPersons, Long departure, Long arrival) {

        // TODO: Logik, damit Plätze mit entsprechenden Eigenschaften gewählt

        /*
         * Wir wollen ja immer einen Platz ausgeben, sofern es genug Plätze gibt. Deswegen ruft getSeat einfach
         * getAllSeats auf und gibt dann nur eine Teilmenge zurück
         * 
         * Die Teilmenge umfasst, wie viele Plätze gebucht werden sollten (numberOfPersons)
         * 
         * An dieser Stelle bräuchte es ggf. Sonderlogik, nach der wir die möglichst passenden Plätze auswählen
         * 
         * Wenn wir direkt nach den Eigenschaften fragen würden, würden wir aber potentiell keinen passenden Platz
         * finden, müssten dann noch eine Anfrage mit weniger Eigenschaften stellen usw. und das ist viel Traffic hin
         * und her. Daher das TODO
         */
        List<Seat> seats = this.getAllAvailableSeats(trainConnectionID, departure, arrival);

        List<Seat> recommendation = new ArrayList<Seat>();
        List<SeatAllocation> allocations = new ArrayList<SeatAllocation>();

        int count = 0;

        while (true) {
            for (Seat seat : seats) {
                if (seat.getSeatClass() == seatClass) {

                    recommendation.add(seat);
                    allocations.add(new SeatAllocation(seat.getId(), trainConnectionID, departure, arrival));
                    count++;

                }
                if (count == numberOfPersons) {
                    if (EventStoreConnection.lockSeat(allocations)) {
                        return recommendation;
                    } else {
                        // TODO: nur die clearen, bei denen es einen Konflikt
                        // gab
                        allocations.clear();
                        recommendation.clear();
                        count = 0;
                    }
                }
            }
            return null;
        }

    }

    @Override
    public List<Seat> getAllAvailableSeats(int trainConnectionID, Long departure, Long arrival) {

        List<Seat> seats = new ArrayList<Seat>();

        Connection conn = null;
        try {
            /*
             * Diese Query gibt uns alle freien Plätze
             * 
             * Was sie tut ist: 1. Suche alle Plätze, die zur Zugfahrt passen 2. Entfernt die Plätze, die für diesen Tag
             * und diese Zeit bereits belegt sind (umfasst auch Plätze, die nur zwischendrin oder abschnittsweise eine
             * Überschneidung haben)
             */
            String getSeats = IOUtils.toString(classLoader.getResourceAsStream("Queries/get-available-seats"),
                    "UTF-8");

            String getSeatInformation = "SELECT * FROM seat where id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement getSeatsStmt = conn.prepareStatement(getSeats);

            getSeatsStmt.setInt(1, trainConnectionID);
            getSeatsStmt.setInt(2, trainConnectionID);
            getSeatsStmt.setLong(3, departure);
            getSeatsStmt.setLong(4, arrival);
            getSeatsStmt.setLong(5, departure);
            getSeatsStmt.setLong(6, arrival);
            getSeatsStmt.setLong(7, departure);
            getSeatsStmt.setLong(8, arrival);

            logger.info("Executing query " + getSeats);
            ResultSet seatsRS = getSeatsStmt.executeQuery();

            PreparedStatement getSeatInformationStmt = conn.prepareStatement(getSeatInformation);

            /*
             * Für jede zulässige ID werden dann die anderen Informationen zum Platz gesucht und ein neues Objekt
             * erstellt
             */
            while (seatsRS.next()) {

                getSeatInformationStmt.setInt(1, seatsRS.getInt("seat_id"));

                ResultSet seat = getSeatInformationStmt.executeQuery();
                if (seat.next()) {
                    seats.add(new Seat(seat.getInt("id"), seat.getInt("class"), seat.getString("area"), seat
                            .getString("location"), seat.getString("compartment_type"), seat
                            .getBoolean("upper_level"), seat.getInt("wagon_id")));
                }

            }
            getSeatsStmt.close();
            getSeatInformationStmt.close();

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                }
            }
        }
        return seats;
    }

    // TODO: Event Store Logik implementieren. Das macht an dieser Stelle keinen
    // Sinn, weil wir gar nicht in MySQL sind..
    @Override
    public boolean lockSeat(int seatID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean unlockSeat(int seatID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Seat> getAllSeats(int trainConnectionID, Long departure, Long arrival) {
        List<Seat> seats = new ArrayList<Seat>();

        Connection conn = null;
        try {
            /*
             * Diese Query gibt uns alle freien Plätze
             * 
             * Was sie tut ist: 1. Suche alle Plätze, die zur Zugfahrt passen 2. Entfernt die Plätze, die für diesen Tag
             * und diese Zeit bereits belegt sind (umfasst auch Plätze, die nur zwischendrin oder abschnittsweise eine
             * Überschneidung haben)
             */
            String getSeats = IOUtils.toString(classLoader.getResourceAsStream("Queries/get-all-seats"), "UTF-8");

            String getSeatInformation = "SELECT * FROM seat where id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement getSeatsStmt = conn.prepareStatement(getSeats);

            getSeatsStmt.setInt(1, trainConnectionID);
            logger.info("Executing query " + getSeats);
            ResultSet seatsRS = getSeatsStmt.executeQuery();

            PreparedStatement getSeatInformationStmt = conn.prepareStatement(getSeatInformation);

            /*
             * Für jede zulässige ID werden dann die anderen Informationen zum Platz gesucht und ein neues Objekt
             * erstellt
             */
            while (seatsRS.next()) {

                getSeatInformationStmt.setInt(1, seatsRS.getInt("seat_id"));

                ResultSet seat = getSeatInformationStmt.executeQuery();
                if (seat.next()) {
                    seats.add(new Seat(seat.getInt("id"), seat.getInt("class"), seat.getString("area"), seat
                            .getString("location"), seat.getString("compartment_type"), seat
                            .getBoolean("upper_level"), seat.getInt("wagon_id")));
                }

            }
            getSeatsStmt.close();
            getSeatInformationStmt.close();

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                }
            }
        }
        return seats;
    }

}
