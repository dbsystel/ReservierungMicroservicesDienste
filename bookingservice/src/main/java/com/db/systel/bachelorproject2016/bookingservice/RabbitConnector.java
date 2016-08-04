package com.db.systel.bachelorproject2016.bookingservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.BookingInitiatedListener;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.BookingQueueFeeder;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.ConnectionCancelledListener;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.SeatDisabledListener;
import com.db.systel.bachelorproject2016.bookingservice.domainmodel.logic.ShutdownConnectionListener;

public class RabbitConnector implements Runnable {

    @Override
    public void run() {

        //Erst die Verbindung herstellen und anmelden
        while (!BookingService.connectedToRabbit) {
            connect("rabbit", 5672);
        }

        //Dann die Listener initialisieren
        setListeners();
    }

    public void connect(String rabbitIP, Integer rabbitPort) {
        try {

            //Verbindung herstellen
            System.out.println("Conntecting to " + rabbitIP + ":" + rabbitPort);

            //Setzt Host, Port und Nutzer der Factory
            BookingService.rabbitConnFactory = new CachingConnectionFactory();
            BookingService.rabbitConnFactory.setHost(rabbitIP);
            BookingService.rabbitConnFactory.setPort(rabbitPort);
            BookingService.rabbitConnFactory.setVirtualHost("host_db");
            BookingService.rabbitConnFactory.setUsername("booking");
            BookingService.rabbitConnFactory.setPassword("booking");
            BookingService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

            //Stellt den Admin und das Template zum Senden der Nachrichten auf 
            BookingService.admin = new RabbitAdmin(BookingService.rabbitConnFactory);
            BookingService.template = new RabbitTemplate(BookingService.rabbitConnFactory);

            BookingService.queueFeeder = new BookingQueueFeeder();

            BookingService.connectedToRabbit = true;
        }

        catch (Exception e) {
            BookingService.logger.error("Error while trying to connect to rabbit: " + e.getMessage());
        }
    }

    public static void setListeners() {

        //Entfernt nach 10 Minunten Buchungsschutz
        ArrayList<List<String>> bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "initiate"));

        initListener("booking.unguard_booking", bindings, new BookingInitiatedListener(), 600000);

        //Hört auf ausfallende Züge / Wägen --> invalidiert Buchung

        bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("train_connection", "cancel"));

        initListener("booking.invalidate_booking_for_connection", bindings, new ConnectionCancelledListener(), 0);

        bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("seats", "disable"));
        bindings.add(Arrays.asList("wagon", "disable"));

        initListener("booking.invalidate_bookings_for_seats", bindings, new SeatDisabledListener(), 0);
    }

    public static void initListener(String queueName, ArrayList<List<String>> bindings, Object listener, Integer time) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                BookingService.rabbitConnFactory);

        // Wenn die Nachrichten erst nach Ablauf einer Zeit gesendet werden
        // sollen:
        if (time > 0) {
            // Zum Zwischenspeichern der Nachrichten wird ein weiterer Exchange
            // benötigt
            TopicExchange storageExchange = new TopicExchange("storage." + queueName);
            BookingService.admin.declareExchange(storageExchange);

            // Queue für den storage declaren -- Nachrichen werden nach Ablauf
            // der Zeit an den Exchange für die richtige Queue gesendet
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("x-message-ttl", time);
            header.put("x-dead-letter-exchange", "storage." + queueName);
            Queue storageQueue = new Queue("storage." + queueName, true, false, false, header);
            BookingService.admin.declareQueue(storageQueue);

            Queue queue = new Queue(queueName, true);
            BookingService.admin.declareQueue(queue);

            System.out.println("declared queue");
            // Die Storage-Schlange wird an die Exchanges gebunden
            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                BookingService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    BookingService.admin.declareBinding(BindingBuilder.bind(storageQueue).to(tEx)
                            .with(binding.get(i)));
                    BookingService.admin.declareBinding(BindingBuilder.bind(queue).to(storageExchange)
                            .with(binding.get(i)));
                }
            }

            // Nun wid die richtige Schlange initialisiert, die an den
            // Storage-Exchange gebunden wird. Diese erhält nun alle Nachrichten
            // nach Ablauf der Zeit t

        } else {

            //Ansonsten wird einfach nur die Warteschlange deklariert 
            Queue queue = new Queue(queueName, true);
            BookingService.admin.declareQueue(queue);

            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                BookingService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    BookingService.admin.declareBinding(BindingBuilder.bind(queue).to(tEx).with(binding.get(i)));
                }
            }
        }

        //Listener wird hinzugefügt
        container.addQueueNames(queueName);
        container.setMessageListener(listener);
        container.start();
    }
}
