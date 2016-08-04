package com.db.systel.bachelorproject2016.pricingservice;

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

import com.db.systel.bachelorproject2016.pricingservice.domainmodel.DecreaseCountListener;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.IncreaseCountListener;
import com.db.systel.bachelorproject2016.pricingservice.domainmodel.ShutdownConnectionListener;

public class RabbitConnector implements Runnable {

    public void run() {

        while (!PricingService.connectedToRabbit) {
            connect("rabbit", 5672);
        }

        setListeners();
    }

    public void connect(String rabbitIP, Integer rabbitPort) {

        //Setzt Host, Port etc. der Factory
        System.out.println("Conntecting to " + rabbitIP + ":" + rabbitPort);
        PricingService.rabbitConnFactory = new CachingConnectionFactory();
        PricingService.rabbitConnFactory.setHost(rabbitIP);
        PricingService.rabbitConnFactory.setPort(rabbitPort);
        PricingService.rabbitConnFactory.setVirtualHost("host_db");
        PricingService.rabbitConnFactory.setUsername("pricing");
        PricingService.rabbitConnFactory.setPassword("pricing");
        PricingService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

        //Deklariert Admin und Template auf Grundlage der Factory
        PricingService.admin = new RabbitAdmin(PricingService.rabbitConnFactory);
        PricingService.template = new RabbitTemplate(PricingService.rabbitConnFactory);

        PricingService.connectedToRabbit = true;
    }

    public void setListeners() {

        ArrayList<List<String>> bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "delete"));
        initListener("pricing.increase_counter", bindings, new IncreaseCountListener(), 0);

        bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "confirm"));
        initListener("pricing.decrease_counter", bindings, new DecreaseCountListener(), 0);

    }

    //listener --> Objekt, dass das Listener-Interface implentiert und dort durch die Methode "onMessage" deklariert, was passieren soll
    public static void initListener(String queueName, ArrayList<List<String>> bindings, Object listener, Integer time) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                PricingService.rabbitConnFactory);

        // Wenn die Nachrichten erst nach Ablauf einer Zeit gesendet werden
        // sollen:
        if (time > 0) {
            // Zum Zwischenspeichern der Nachrichten wird ein weiterer Exchange
            // benötigt
            TopicExchange storageExchange = new TopicExchange("storage");
            PricingService.admin.declareExchange(storageExchange);

            // Queue für den storage declaren -- Nachrichen werden nach Ablauf
            // der Zeit an den Exchange für die richtige Queue gesendet
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("x-message-ttl", time);
            header.put("x-dead-letter-exchange", "storage");
            Queue storageQueue = new Queue("storage", true, false, false, header);
            PricingService.admin.declareQueue(storageQueue);

            // Die Storage-Schlange wird an die Exchanges gebunden
            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                PricingService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    PricingService.admin.declareBinding(BindingBuilder.bind(storageQueue).to(tEx)
                            .with(binding.get(i)));
                }
            }

            // Nun wid die richtige Schlange initialisiert, die an den
            // Storage-Exchange gebunden wird. Diese erhält nun alle Nachrichten
            // nach Ablauf der Zeit t
            Queue queue = new Queue(queueName, true);
            PricingService.admin.declareBinding(BindingBuilder.bind(queue).to(storageExchange).with(".*"));

        } else {

            //Ansonsten wird einfach nur die Warteschlange deklariert 
            Queue queue = new Queue(queueName, true);
            PricingService.admin.declareQueue(queue);

            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                PricingService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    PricingService.admin.declareBinding(BindingBuilder.bind(queue).to(tEx).with(binding.get(i)));
                }
            }
        }

        //Listener wird hinzugefügt
        container.addQueueNames(queueName);
        container.setMessageListener(listener);
        container.start();
    }
}
