package com.db.systel.bachelorproject2016.customermanagementservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;

import com.db.systel.bachelorproject2016.customermanagementservice.api.CustomerManagementController;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.CustomerManagementDAO;

/*
 * TODO: 
 * 
 * 
 * Accounts löschen
 * Sicherheit
 * 
 * Error handling:
 * 	Eingaben nicht korrekt
 *  Nutzername schon vergeben
 *  Ein Kunde mit solchen Daten existiert bereits --> haben Sie vielleicht einen Account?
 */

public class CustomerManagementService {

    public static final Logger logger = LoggerFactory.getLogger(CustomerManagementService.class);

    public static CustomerManagementDAO customerManagementDAO;

    public static boolean connectedToMySQL;

    public static CachingConnectionFactory rabbitConnFactory;

    public static RabbitAdmin admin;

    public static RabbitTemplate template;

    public static boolean connectedToRabbit;

    public static void main(String args[]) {

        connectedToMySQL = false;

        connectedToRabbit = false;

        //Startet das Thread, das die Verbindung herstellt

        //Es werden einzelne Threads verwendet, damit das gesamte Hochfahren nicht blockiert wird 
        Thread connectToSQL = new Thread(new MySQLConnector());
        connectToSQL.start();

        Thread connectToRabbit = new Thread(new RabbitConnector());
        connectToRabbit.start();

        SpringApplication.run(CustomerManagementController.class, args);

    }

}