package com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.DatabaseObject;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Route;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.RouteStation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Seat;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.SeatAllocation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Station;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Train;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.TrainConnection;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.TrainConnectionCancellation;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.Wagon;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.data.WagonTrainConnection;
import com.mysql.jdbc.Statement;

public class JdbcSeatManagementDAO implements SeatManagementDAO {

    private static final Logger logger = LoggerFactory.getLogger(JdbcSeatManagementDAO.class);

    private DataSource dataSource;

    private ClassLoader classLoader = getClass().getClassLoader();

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // TODO: Man könnte hier die genereatedKeys die ID des neu hinzugefügten
    // Elements zurück geben lassen, wäre vielleicht gut fürs Testing
    public Integer insert(String type, DatabaseObject databaseObject) {

        Integer insertionID = null;
        Connection conn = null;
        String insert = null;
        try {

            /*
             * Baue die Query dynamisch zusammen, in Abhängigkeit davon, wie viele Properties es gibt
             */

            insert = "INSERT INTO " + type + "(";

            String questionMarks = "(";

            for (String attribute : databaseObject.columnAttributes) {
                insert += attribute + ", ";
                questionMarks += "?, ";
            }
            /*
             * ggf. letztes Komma weg machen
             */
            if (databaseObject.columnAttributes.length >= 1) {
                questionMarks = questionMarks.substring(0, questionMarks.length() - 2) + ")";
                insert = insert.substring(0, insert.length() - 2) + ")";
            } else {
                questionMarks += ")";
                insert += ")";
            }

            insert += "VALUES " + questionMarks + ";";

            conn = dataSource.getConnection();
            PreparedStatement insertStmt = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);

            /*
             * Das Datenbankobjekt teilt uns ja nun mit, auf welche Properties wir zugreifen können. D.h. wir nehmen das
             * Objekt und suchen uns die Properties mit den festgelegten Namen (z.B. routeID). Dadurch müssen wir nicht
             * explizit getRouteID() etc. aufrufen, sondern können generisch vorgehen
             */
            for (int i = 0; i < databaseObject.properties.length; i++) {

                try {
                    /*
                     * Die Werte der Properties werden dann in das PreparedStatement gesetzt. Wichtig dafür ist, dass
                     * die Reihenfolge der Properties im Array gleich den Attributen in der SQL-Anfrage sind
                     */
                    Class<?> c = databaseObject.getClass();
                    Field f = c.getDeclaredField(databaseObject.properties[i]);
                    f.setAccessible(true);
                    insertStmt.setObject(i + 1, f.get(databaseObject));

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    logger.error(e.getMessage());
                    //System.out.println(e);
                    e.printStackTrace();

                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e1) {
                            logger.debug(e.getMessage());
                        }
                    }

                    return null;
                }
            }

            insertStmt.executeUpdate();

            ResultSet keyset = insertStmt.getGeneratedKeys();

            keyset.next();

            insertionID = keyset.getInt(1);

            insertStmt.close();

        } catch (SQLException e) {
            //System.out.println(e);
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
        return insertionID;
    }

    @Override
    public DatabaseObject update(Integer id, String type, DatabaseObject databaseObject) {
        Connection conn = null;
        DatabaseObject obj = null;

        String select = "SELECT * FROM " + type + " WHERE id = ?;";

        try {

            String update = "UPDATE " + type + " SET ";
            conn = dataSource.getConnection();

            PreparedStatement selectStmt = conn.prepareStatement(select);
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                obj = null;
                return obj;
            }

            /*
             * Auch hier wieder die Properties anhand ihres Namens generisch ermittelt und in das Statement schreiben.
             * Genauer beschrieben bei Insert
             */
            for (int i = 0; i < databaseObject.properties.length; i++) {

                try {
                    Class<?> c = databaseObject.getClass();
                    Field f = c.getDeclaredField(databaseObject.properties[i]);
                    f.setAccessible(true);

                    /*
                     * Wenn der Wert null ist, soll keine Änderung statt finden
                     */
                    if (f.get(databaseObject) != null) {

                        update += databaseObject.columnAttributes[i] + " = ";
                        if (f.getGenericType().equals(String.class)) {
                            update += "'" + f.get(databaseObject) + "' ,";
                        } else {
                            update += f.get(databaseObject) + " ,";
                        }
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage());
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException e1) {
                            logger.debug(e.getMessage());
                        }
                    }

                    return null;
                }

            }

            update = update.substring(0, update.length() - 2) + " ";
            update += " WHERE id = ?;";

            PreparedStatement updateStmt = conn.prepareStatement(update);
            updateStmt.setInt(1, id);
            updateStmt.executeUpdate();
            updateStmt.close();

            obj = select(id, type);

        } catch (SQLException e) {
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
        return obj;
    }

    @Override
    public boolean delete(Integer id, String type) {
        String delete = "DELETE FROM " + type + " WHERE id = ?;";

        String select = "SELECT * FROM " + type + " WHERE id = ?;";
        Connection conn = null;

        boolean couldDelete = false;

        try {
            conn = dataSource.getConnection();

            PreparedStatement selectStmt = conn.prepareStatement(select);
            selectStmt.setInt(1, id);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement deleteStmt = conn.prepareStatement(delete);
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();
                deleteStmt.close();
                couldDelete = true;
            }
            selectStmt.close();

        } catch (SQLException e) {
            logger.debug(e.getMessage());
            //System.out.println(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                    //System.out.println(e.getMessage());
                }
            }

        }
        return couldDelete;
    }

    @Override
    public List<Integer> disable(Integer id, String type) {
        List<Integer> seatIDs = new ArrayList<Integer>();

        Connection conn = null;

        try {
            /*
             * Wenn ein Platz oder Wagen disabled wird, dann wollen wir ein Update durchführen (State = ...) und wir
             * wollen alle Plätze ermitteln, die davon betroffen sind. Deswegen 2 Anfragen
             */
            String disable = IOUtils.toString(classLoader.getResourceAsStream("Queries/disable/" + type), "UTF-8");

            String selectSeatIDs = IOUtils.toString(classLoader.getResourceAsStream("Queries/select/" + type),
                    "UTF-8");

            /*
             * Erst das Update durchführen
             */
            conn = dataSource.getConnection();
            PreparedStatement disableStmt = conn.prepareStatement(disable);
            disableStmt.setInt(1, id);
            disableStmt.executeUpdate();
            disableStmt.close();

            PreparedStatement selectStmt = conn.prepareStatement(selectSeatIDs);
            selectStmt.setInt(1, id);
            ResultSet seatIdRS = selectStmt.executeQuery();

            String disableSeats = IOUtils.toString(classLoader.getResourceAsStream("Queries/disable/seat"), "UTF-8");
            PreparedStatement disableSeatsStmt = null;
            while (seatIdRS.next()) {

                disableSeatsStmt = conn.prepareStatement(disableSeats);
                disableSeatsStmt.setInt(1, seatIdRS.getInt(1));
                disableSeatsStmt.executeUpdate();
                seatIDs.add(seatIdRS.getInt(1));
            }
            seatIdRS.close();

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
        return seatIDs;
    }

    @Override
    public DatabaseObject select(Integer id, String type) {
        /*
         * Auswählen eines Objekts
         */
        String select = "SELECT * FROM " + type + " WHERE id = ?;";

        DatabaseObject databaseObject = null;

        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(select);
            selectStmt.setInt(1, id);

            ResultSet RS = selectStmt.executeQuery();

            /*
             * Je nachdem, welcher Typ angegeben ist, wird ein konkretes Datenbankobjekt erstellt
             */
            if (RS.next()) {

                switch (type) {
                case "route":
                    databaseObject = new Route();
                    break;
                case "station":
                    databaseObject = new Station(null);
                    break;
                case "route_station":
                    databaseObject = new RouteStation(null, null, null, null);
                    break;
                case "seat":
                    databaseObject = new Seat(null, null, null, null, false, null, null);
                    break;
                case "wagon":
                    databaseObject = new Wagon(null);
                    break;
                case "train":
                    databaseObject = new Train(null, null);
                    break;
                case "train_connection":
                    databaseObject = new TrainConnection(null, null, null, null);
                    break;
                case "wagon_train_connection":
                    databaseObject = new WagonTrainConnection(null, null, null);
                    break;
                case "seat_allocation":
                    databaseObject = new SeatAllocation(null, null, null, null);
                    break;
                case "train_connection_cancellation":
                    databaseObject = new TrainConnectionCancellation(null, null);
                    break;
                default:
                    return null;
                }
                for (int i = 0; i < databaseObject.properties.length; i++) {

                    try {
                        /*
                         * Die Werte der Properties werden dann in das Objekt gesetzt
                         */
                        Class<?> c = databaseObject.getClass();
                        Field f = c.getDeclaredField(databaseObject.properties[i]);
                        f.setAccessible(true);

                        /*
                         * Explizit angeben, was wir gerne auslesen wollen.
                         * 
                         * Ist grundsätzlich nicht nötig, vermeidet aber ggf. Errors
                         */
                        if (f.getGenericType().equals(Long.class)) {
                            f.set(databaseObject, RS.getLong(databaseObject.columnAttributes[i]));

                        } else if (f.getGenericType().equals(Boolean.class)) {
                            f.set(databaseObject, RS.getBoolean(databaseObject.columnAttributes[i]));
                        } else if (f.getGenericType().equals(Integer.class)) {
                            f.set(databaseObject, RS.getInt(databaseObject.columnAttributes[i]));
                        } else {
                            f.set(databaseObject, RS.getObject(databaseObject.columnAttributes[i]));
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        //System.out.println(e);
                        e.printStackTrace();
                    }
                }
            } else {
                databaseObject = null;
            }
            selectStmt.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            //System.out.println(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                    //System.out.println(e.getMessage());
                }
            }

        }

        return databaseObject;
    }

    @Override
    public boolean deleteSeatAllocation(SeatAllocation seatAllocation) {

        String whereClause = "train_connection_id = ? AND seat_id = ? AND allocated_from = ? AND allocated_until = ?;";

        String delete = "DELETE FROM seat_allocation WHERE " + whereClause;
        String select = "SELECT * FROM seat_allocation WHERE " + whereClause;
        Connection conn = null;

        boolean couldDelete = false;

        try {
            conn = dataSource.getConnection();

            PreparedStatement selectStmt = conn.prepareStatement(select);

            selectStmt.setInt(1, seatAllocation.getTrainConnectionId());
            selectStmt.setInt(2, seatAllocation.getSeatId());
            selectStmt.setLong(3, seatAllocation.getDepartureTime());
            selectStmt.setLong(4, seatAllocation.getArrivalTime());

            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement deleteStmt = conn.prepareStatement(delete);

                deleteStmt.setInt(1, seatAllocation.getTrainConnectionId());
                deleteStmt.setInt(2, seatAllocation.getSeatId());
                deleteStmt.setLong(3, seatAllocation.getDepartureTime());
                deleteStmt.setLong(4, seatAllocation.getArrivalTime());

                deleteStmt.executeUpdate();
                deleteStmt.close();
                couldDelete = true;
            }
            selectStmt.close();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            //System.out.println(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                    //System.out.println(e.getMessage());
                }
            }

        }
        return couldDelete;
    }

    @Override
    public SeatAllocation updateSeatAllocation(SeatAllocation origin, SeatAllocation newInformation) {

        String whereClause = "train_connection_id = ? AND seat_id = ? AND allocated_from = ? AND allocated_until = ?;";

        String update = "UPDATE seat_allocation SET train_connection_id = ?, seat_id = ?, allocated_from = ?, allocated_until =? "
                + "WHERE " + whereClause;
        String select = "SELECT * FROM seat_allocation WHERE " + whereClause;
        Connection conn = null;

        Integer updateID = null;

        try {
            conn = dataSource.getConnection();

            PreparedStatement selectStmt = conn.prepareStatement(select);

            selectStmt.setInt(1, origin.getTrainConnectionId());
            selectStmt.setInt(2, origin.getSeatId());
            selectStmt.setLong(3, origin.getDepartureTime());
            selectStmt.setLong(4, origin.getArrivalTime());

            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                PreparedStatement updateStmt = conn.prepareStatement(update);

                updateStmt.setInt(1, newInformation.getTrainConnectionId());
                updateStmt.setInt(2, newInformation.getSeatId());
                updateStmt.setLong(3, newInformation.getDepartureTime());
                updateStmt.setLong(4, newInformation.getArrivalTime());

                updateStmt.setInt(5, origin.getTrainConnectionId());
                updateStmt.setInt(6, origin.getSeatId());
                updateStmt.setLong(7, origin.getDepartureTime());
                updateStmt.setLong(8, origin.getArrivalTime());

                updateStmt.executeUpdate();

                updateID = rs.getInt("id");

                updateStmt.close();
            } else {
                selectStmt.close();
                return null;
            }
            selectStmt.close();

        } catch (SQLException e) {
            logger.debug(e.getMessage());
            //System.out.println(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                    //System.out.println(e.getMessage());
                }
            }

        }
        DatabaseObject seatAlloction = this.select(updateID, "seat_allocation");

        return (SeatAllocation) seatAlloction;
    }

}