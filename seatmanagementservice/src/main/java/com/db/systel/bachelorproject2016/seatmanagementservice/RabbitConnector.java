package com.db.systel.bachelorproject2016.seatmanagementservice;

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

import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.BookingQueueConfirmedListener;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.BookingQueueDeletedListener;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.SeatManagementQueueFeeder;
import com.db.systel.bachelorproject2016.seatmanagementservice.domainmodel.logic.ShutdownConnectionListener;

public class RabbitConnector implements Runnable {

    @Override
    public void run() {

        while (!SeatManagementService.connectedToRabbit) {
            connect("rabbit", 5672);
        }
        setListener();
    }

    public void connect(String rabbitIP, Integer rabbitPort) {

        //Setzt Hosts und Ports der Verbindung 
        SeatManagementService.rabbitConnFactory = new CachingConnectionFactory();
        SeatManagementService.rabbitConnFactory.setHost(rabbitIP);
        SeatManagementService.rabbitConnFactory.setPort(rabbitPort);
        SeatManagementService.rabbitConnFactory.setVirtualHost("host_db");
        SeatManagementService.rabbitConnFactory.setUsername("seatmanagement");
        SeatManagementService.rabbitConnFactory.setPassword("seatmanagement");
        SeatManagementService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

        //Ersstellt Admin und Template für den Zugriff auf die Warteschlangen 

        SeatManagementService.admin = new RabbitAdmin(SeatManagementService.rabbitConnFactory);
        SeatManagementService.template = new RabbitTemplate(SeatManagementService.rabbitConnFactory);

        SeatManagementService.queueFeeder = new SeatManagementQueueFeeder();
        SeatManagementService.connectedToRabbit = true;
    }

    public void setListener() {
        ArrayList<List<String>> bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "delete"));
        initListener("seatmanagement.deallocate_seat", bindings, new BookingQueueDeletedListener(), 0);

        bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "confirm"));
        initListener("seatmanagement.allocate_seat", bindings, new BookingQueueConfirmedListener(), 0);
    }

    public static void initListener(String queueName, ArrayList<List<String>> bindings, Object listener, Integer time) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                SeatManagementService.rabbitConnFactory);

        // Wenn die Nachrichten erst nach Ablauf einer Zeit gesendet werden
        // sollen:
        if (time > 0) {
            // Zum Zwischenspeichern der Nachrichten wird ein weiterer Exchange
            // benötigt
            TopicExchange storageExchange = new TopicExchange("storage");
            SeatManagementService.admin.declareExchange(storageExchange);

            // Queue für den storage declaren -- Nachrichen werden nach Ablauf
            // der Zeit an den Exchange für die richtige Queue gesendet
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("x-message-ttl", time);
            header.put("x-dead-letter-exchange", "storage");
            Queue storageQueue = new Queue("storage", true, false, false, header);
            SeatManagementService.admin.declareQueue(storageQueue);

            // Die Storage-Schlange wird an die Exchanges gebunden
            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                SeatManagementService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    SeatManagementService.admin.declareBinding(BindingBuilder.bind(storageQueue).to(tEx)
                            .with(binding.get(i)));
                }
            }

            // Nun wid die richtige Schlange initialisiert, die an den
            // Storage-Exchange gebunden wird. Diese erhält nun alle Nachrichten
            // nach Ablauf der Zeit t
            Queue queue = new Queue(queueName, true);
            SeatManagementService.admin.declareBinding(BindingBuilder.bind(queue).to(storageExchange).with(".*"));

        } else {

            //Ansonsten wird einfach nur die Warteschlange deklariert 
            Queue queue = new Queue(queueName, true);
            SeatManagementService.admin.declareQueue(queue);

            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                SeatManagementService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    SeatManagementService.admin.declareBinding(BindingBuilder.bind(queue).to(tEx)
                            .with(binding.get(i)));
                }
            }
        }

        //Listener wird hinzugefügt
        container.addQueueNames(queueName);
        container.setMessageListener(listener);
        container.start();
    }
}
