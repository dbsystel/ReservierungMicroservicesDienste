package com.db.systel.bachelorproject2016.customermanagementservice;

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

import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.BookingInvalidatedListener;
import com.db.systel.bachelorproject2016.customermanagementservice.domainmodel.logic.ShutdownConnectionListener;

public class RabbitConnector implements Runnable {

    @Override
    public void run() {

        while (!CustomerManagementService.connectedToRabbit) {
            connect("rabbit", 5672);
        }

        ArrayList<List<String>> bindings = new ArrayList<List<String>>();
        bindings.add(Arrays.asList("booking", "invalidate"));

        initListener("customermanagement.notify_customer", bindings, new BookingInvalidatedListener(), 0);

    }

    public void connect(String rabbitIP, Integer rabbitPort) {
        try {
            System.out.println("Conntecting to " + rabbitIP + ":" + rabbitPort);

            //Setzt Host, Port und Nutzer der Factory
            CustomerManagementService.rabbitConnFactory = new CachingConnectionFactory();
            CustomerManagementService.rabbitConnFactory.setHost(rabbitIP);
            CustomerManagementService.rabbitConnFactory.setPort(rabbitPort);
            CustomerManagementService.rabbitConnFactory.setVirtualHost("host_db");
            CustomerManagementService.rabbitConnFactory.setUsername("customermanagement");
            CustomerManagementService.rabbitConnFactory.setPassword("customermanagement");
            CustomerManagementService.rabbitConnFactory.addConnectionListener(new ShutdownConnectionListener());

            //Stellt den Admin und das Template zum Senden der Nachrichten auf 
            CustomerManagementService.admin = new RabbitAdmin(CustomerManagementService.rabbitConnFactory);
            CustomerManagementService.template = new RabbitTemplate(CustomerManagementService.rabbitConnFactory);

            CustomerManagementService.connectedToRabbit = true;
        }

        catch (Exception e) {
            CustomerManagementService.logger.error("Error while trying to connect to rabbit: " + e.getMessage());
        }
    }

    public static void initListener(String queueName, ArrayList<List<String>> bindings, Object listener, Integer time) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
                CustomerManagementService.rabbitConnFactory);

        // Wenn die Nachrichten erst nach Ablauf einer Zeit gesendet werden
        // sollen:
        if (time > 0) {
            // Zum Zwischenspeichern der Nachrichten wird ein weiterer Exchange
            // benötigt
            TopicExchange storageExchange = new TopicExchange("storage");
            CustomerManagementService.admin.declareExchange(storageExchange);

            // Queue für den storage declaren -- Nachrichen werden nach Ablauf
            // der Zeit an den Exchange für die richtige Queue gesendet
            Map<String, Object> header = new HashMap<String, Object>();
            header.put("x-message-ttl", time);
            header.put("x-dead-letter-exchange", "storage");
            Queue storageQueue = new Queue("storage", true, false, false, header);
            CustomerManagementService.admin.declareQueue(storageQueue);

            // Die Storage-Schlange wird an die Exchanges gebunden
            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                CustomerManagementService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    CustomerManagementService.admin.declareBinding(BindingBuilder.bind(storageQueue).to(tEx)
                            .with(binding.get(i)));
                }
            }

            // Nun wid die richtige Schlange initialisiert, die an den
            // Storage-Exchange gebunden wird. Diese erhält nun alle Nachrichten
            // nach Ablauf der Zeit t
            Queue queue = new Queue(queueName, true);
            CustomerManagementService.admin.declareBinding(BindingBuilder.bind(queue).to(storageExchange).with(".*"));

        } else {

            //Ansonsten wird einfach nur die Warteschlange deklariert 
            Queue queue = new Queue(queueName, true);
            CustomerManagementService.admin.declareQueue(queue);

            for (List<String> binding : bindings) {
                TopicExchange tEx = new TopicExchange(binding.get(0));
                CustomerManagementService.admin.declareExchange(tEx);
                for (int i = 1; i < binding.size(); i++) {
                    CustomerManagementService.admin.declareBinding(BindingBuilder.bind(queue).to(tEx)
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
