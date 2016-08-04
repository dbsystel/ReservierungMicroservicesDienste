package com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic;

import java.io.IOException;
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

import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.Account;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.Customer;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.data.CustomerContact;
import com.mysql.jdbc.Statement;

public class JdbcCustomerManagementDAO implements CustomerManagementDAO {

    private static final Logger logger = LoggerFactory.getLogger(JdbcCustomerManagementDAO.class);

    private DataSource dataSource;

    // Brauchen wir zum Auslesen der Dateien
    private ClassLoader classLoader = getClass().getClassLoader();

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Customer getCustomer(String username, String password, boolean fullInformation) {

        String getCustomer = null;
        Connection conn = null;
        /*
         * Je nachdem, ob alle Informationen gewünscht sind (z.B. Anfrage für eine Buchung, damit man die Daten nicht
         * selbst eingeben muss, wird nur die Kunden-ID oder alles ausgelesen
         * 
         * Customer hat zwei Konstruktoren die dazu passen
         */
        if (!fullInformation) {

            try {

                /*
                 * Die SQL-Queries liegen in einem eigenen Ordner und werden hier ausgelesen
                 */
                getCustomer = IOUtils
                        .toString(classLoader.getResourceAsStream("Queries/select_customer_id"), "UTF-8");

                /*
                 * Verbindung wird aufgebaut und die Parameter für das Statement gesetzt
                 */
                conn = dataSource.getConnection();
                PreparedStatement getCustomerStmt = conn.prepareStatement(getCustomer);

                getCustomerStmt.setString(1, password);
                getCustomerStmt.setString(2, username);

                /*
                 * Durch das Ausführen der Query erhält man ein Resultset zurück, auf dessen Grundlage nun der Kunde
                 * erstellt wird
                 * 
                 * Dieser Ablauf ist für alle SQL Anfragen gleich, wird also nicht überall nochmal kommentiert
                 */

                System.out.println("Executing query: " + getCustomer);

                ResultSet customerRS = getCustomerStmt.executeQuery();
                if (customerRS.next()) {
                    return new Customer(customerRS.getInt("customer_id"));
                } else {
                    return null;
                }

            } catch (SQLException | IOException e) {
                logger.error(e.getMessage());
                //System.out.print(e.getMessage());
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
        } else {
            try {
                getCustomer = IOUtils.toString(classLoader.getResourceAsStream("Queries/select_customer"), "UTF-8");

                conn = dataSource.getConnection();
                PreparedStatement getCustomerStmt = conn.prepareStatement(getCustomer);

                getCustomerStmt.setString(1, password);
                getCustomerStmt.setString(2, username);

                //System.out.println("Executing query: " + getCustomer);
                logger.info("Executing query: " + getCustomer);

                ResultSet customerRS = getCustomerStmt.executeQuery();
                if (customerRS.next()) {
                    return new Customer(customerRS.getInt("customer.id"),
                            customerRS.getString("customer.first_name"), customerRS.getString("last_name"),
                            customerRS.getString("street"), customerRS.getInt("house_number"),
                            customerRS.getInt("post_code"), customerRS.getString("city"),
                            customerRS.getString("e_mail_address"), customerRS.getString("payment_method"));

                } else {
                    return null;
                }

            } catch (SQLException | IOException e) {
                logger.error(e.getMessage());
                System.out.println(e.getMessage());
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
        }
    }

    @Override
    public int insertCustomer(Customer customer) {
        int customerID;

        Connection conn = null;

        try {

            String insertCustomer = IOUtils.toString(classLoader.getResourceAsStream("Queries/insert_customer"),
                    "UTF-8");

            conn = dataSource.getConnection();
            PreparedStatement insertCustomerStmt = conn.prepareStatement(insertCustomer,
                    Statement.RETURN_GENERATED_KEYS);

            insertCustomerStmt.setString(1, customer.getFirstName());
            insertCustomerStmt.setString(2, customer.getLastName());
            insertCustomerStmt.setString(3, customer.getAddress());
            insertCustomerStmt.setInt(4, customer.getHouseNumber());
            insertCustomerStmt.setInt(5, customer.getPostCode());
            insertCustomerStmt.setString(6, customer.getCity());
            insertCustomerStmt.setString(7, customer.geteMailAddress());
            insertCustomerStmt.setString(8, customer.getPaymentMethod());

            //System.out.println("Executing query: " + insertCustomer);
            logger.info("Executing query: " + insertCustomer);
            insertCustomerStmt.executeUpdate();

            // Durch die generierten Schlüssel kann die ID des grade angelegten
            // Kunden ermittelt werden. Diese muss an den Klient zurück
            // geschickt werden
            ResultSet keyset = insertCustomerStmt.getGeneratedKeys();

            keyset.next();
            customerID = keyset.getInt(1);

        } catch (SQLException | IOException e) {
            //System.out.print(e.getMessage());
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
        return customerID;
    }

    @Override
    public String insertAccount(Account account) {

        Connection conn = null;

        try {
            String insertAccount = IOUtils.toString(classLoader.getResourceAsStream("Queries/insert_account"),
                    "UTF-8");

            String checkUsernameExists = "SELECT * FROM account WHERE username = ?;";
            String checkAccountExists = "SELECT * FROM account WHERE customer_id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement insertAccountStmt = conn.prepareStatement(insertAccount);
            PreparedStatement checkUsernameExistsStmt = conn.prepareStatement(checkUsernameExists);
            PreparedStatement checkAccountExistsStmt = conn.prepareStatement(checkAccountExists);

            checkUsernameExistsStmt.setString(1, account.getUsername());
            checkAccountExistsStmt.setInt(1, account.getCustomerID());

            ResultSet username = checkUsernameExistsStmt.executeQuery();

            if (username.next()) {
                return "Username already exists";
            }

            ResultSet customerID = checkAccountExistsStmt.executeQuery();

            if (customerID.next()) {
                return "Customer already has Account";
            }

            insertAccountStmt.setString(1, account.getUsername());
            insertAccountStmt.setString(2, account.getPassword());
            insertAccountStmt.setInt(3, account.getCustomerID());
            insertAccountStmt.executeUpdate();

            //System.out.println("Executing query: " + insertAccount);
            logger.info("Executing query: " + insertAccount);

            insertAccountStmt.close();

        } catch (SQLException | IOException e) {
            //System.out.println(e.getMessage());
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
        return "Account successfully created";
    }

    @Override
    public List<CustomerContact> getContactInformation(List<Integer> customerIDs) {
        List<CustomerContact> customerContacts = new ArrayList<CustomerContact>();

        Connection conn = null;

        try {
            String getContacts = IOUtils.toString(
                    classLoader.getResourceAsStream("Queries/select_customer_contacts"), "UTF-8");

            conn = dataSource.getConnection();
            PreparedStatement getContactsStmt = conn.prepareStatement(getContacts);

            //System.out.println("Executing query: " + getContacts);
            logger.info("Executing query: " + getContacts);

            for (int id : customerIDs) {
                getContactsStmt.setInt(1, id);
                ResultSet contactRS = getContactsStmt.executeQuery();

                if (contactRS.next()) {
                    customerContacts.add(new CustomerContact(id, contactRS.getString("e_mail_address")));
                }
            }
            getContactsStmt.close();

        } catch (SQLException | IOException e) {
            //System.out.println(e.getMessage());
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
        return customerContacts;
    }

    @Override
    public String getEmailAddress(Integer customerID) {

        String address = null;
        Connection conn = null;

        try {
            String getContacts = "SELECT e_mail_address FROM customer WHERE id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement getContactsStmt = conn.prepareStatement(getContacts);

            logger.info("Executing query: " + getContacts);

            getContactsStmt.setInt(1, customerID);
            ResultSet contactRS = getContactsStmt.executeQuery();
            if (contactRS.next()) {
                address = contactRS.getString("e_mail_address");
            }
            getContactsStmt.close();

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
        return address;
    }

    @Override
    public boolean deleteAccount(Integer customerID) {
        Connection conn = null;

        try {

            String deleteAccount = "DELETE FROM account WHERE customer_id = ?;";
            String selectAccount = "SELECT * FROM account WHERE customer_id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement deleteAccountStmt = conn.prepareStatement(deleteAccount);
            PreparedStatement selectAccountStmt = conn.prepareStatement(selectAccount);

            deleteAccountStmt.setInt(1, customerID);
            selectAccountStmt.setInt(1, customerID);

            ResultSet account = selectAccountStmt.executeQuery();

            if (account.next()) {

                deleteAccountStmt.executeUpdate();

                //System.out.println("Executing query: " + deleteAccount);
                logger.info("Executing query: " + deleteAccount);

                deleteAccountStmt.close();
                deleteAccountStmt.close();

                return true;
            }

            return false;

        } catch (SQLException e) {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                }
            }

        }

    }

    @Override
    public boolean deleteCustomer(Integer customerID) {
        Connection conn = null;

        try {

            String deleteCustomer = "DELETE FROM customer WHERE id = ?;";
            String selectCustomer = "SELECT * FROM customer WHERE id = ?;";

            conn = dataSource.getConnection();
            PreparedStatement deleteCustomerStmt = conn.prepareStatement(deleteCustomer);
            PreparedStatement selectCustomerStmt = conn.prepareStatement(selectCustomer);

            deleteCustomerStmt.setInt(1, customerID);
            selectCustomerStmt.setInt(1, customerID);

            ResultSet account = selectCustomerStmt.executeQuery();

            if (account.next()) {

                deleteCustomerStmt.executeUpdate();

                logger.info("Executing query: " + deleteCustomer);

                deleteCustomerStmt.close();
                deleteCustomerStmt.close();

                return true;
            }

            return false;

        } catch (SQLException e) {
            //System.out.println(e.getMessage());
            logger.error(e.getMessage());
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                }
            }

        }

    }

}
