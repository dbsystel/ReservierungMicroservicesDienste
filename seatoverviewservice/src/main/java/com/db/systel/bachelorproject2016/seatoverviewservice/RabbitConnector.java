package com.db.systel.bachelorproject2016.seatoverviewservice;

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

import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.SeatoverviewQueueFeeder;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.ShutdownConnectionListener;
import com.db.systel.bachelorproject2016.seatoverviewservice.domainmodel.logic.UnlockSeatListener;

public class RabbitConnector implements Runnable {

    @Override
    public void run() {

        while (!SeatOverviewService.connectedToRabbit) {
            connect("rabbit", 5672);
        }

        setListener();

    }

    public void connect(String rabbitIP, Integer rabbitPort) {
        try {
            //Setzt Hort und Post für die Factory 
            SeatOverviewService.rabbitConnFactory = new CachingConnectionFactory();
            SeatOverviewService.rabbitConnFactory.setHost(rabbitIP);
            SeatOverviewService.rabbitConnFactory.setPort(rabbitPort);
            SeatOverviewService.rabbitConnFactory.setVirtualHost("host_db");
            SeatOverviewService.rabbitConnFactory.setUsername("seatoverview");
            SeatOverviewService.rabbitConnFactory.setPassword("seatoverview");
            SeatOverviewService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

            //Deklariert Admin und Template, um mit der MessageQueue zu kommunizieren
            SeatOverviewService.admin = new RabbitAdmin(SeatOverviewService.rabbitConnFactory);
            SeatOverviewService.template = new RabbitTemplate(SeatOverviewService.rabbitConnFactory);

            SeatOverviewService.queueFeeder = new SeatoverviewQueueFeeder();

            SeatOverviewService.connectedToRabbit = true;
        } catch (Exception e) {
            System.out.println("Error while trying to connect to rabbit " + e.getMessage());
        }

    }

    public static void setListener() {

        ArrayList<List<String>> bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("seat", "chosen", "suggested"));
        bindings.add(Arrays.asList("booking", "cancel", "delete"));
        initListener("seatoverview.unlock_seat", bindings, new UnlockSeatListener(), 120000);
    }

    public static void initListener(String queueName, ArrayList<List<String>> bindings, Object listener, Integer time) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                SeatOverviewService.rabbitConnFactory);

        // Wenn die Nachrichten erst nach Ablauf einer Zeit gesendet werden
        // sollen:
        if (time > 0) {
            // Zum Zwischenspeichern der Nachrichten wird ein weiterer Exchange
            // benötigt
            TopicExchange storageExchange = new TopicExchange("storage." + queueName);
            SeatOverviewService.admin.declareExchange(storageExchange);

            // Queue für den storage declaren -- Nachrichen werden nach Ablauf
            // der Zeit an den Exchange für die richtige Queue gesendet
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("x-message-ttl", time);
            header.put("x-dead-letter-exchange", "storage." + queueName);
            Queue storageQueue = new Queue("storage." + queueName, true, false, false, header);
            SeatOverviewService.admin.declareQueue(storageQueue);

            Queue queue = new Queue(queueName, true);
            SeatOverviewService.admin.declareQueue(queue);

            System.out.println("declared queue");
            // Die Storage-Schlange wird an die Exchanges gebunden
            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                SeatOverviewService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    SeatOverviewService.admin.declareBinding(BindingBuilder.bind(storageQueue).to(tEx)
                            .with(binding.get(i)));
                    SeatOverviewService.admin.declareBinding(BindingBuilder.bind(queue).to(storageExchange)
                            .with(binding.get(i)));
                }
            }

            // Nun wid die richtige Schlange initialisiert, die an den
            // Storage-Exchange gebunden wird. Diese erhält nun alle Nachrichten
            // nach Ablauf der Zeit t

        } else {

            //Ansonsten wird einfach nur die Warteschlange deklariert 
            Queue queue = new Queue(queueName, true);
            SeatOverviewService.admin.declareQueue(queue);

            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                SeatOverviewService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    SeatOverviewService.admin.declareBinding(BindingBuilder.bind(queue).to(tEx).with(binding.get(i)));
                }
            }
        }

        //Listener wird hinzugefügt
        container.addQueueNames(queueName);
        container.setMessageListener(listener);
        container.start();
    }
}
