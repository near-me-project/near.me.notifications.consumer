package messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.LookupResult;
import shared.Jsons;

import java.util.concurrent.CompletableFuture;

public class RabbitClient {
    private final static Object obj = new Object();
    public static final String MOBILE_NOTIFICATIONS_QUEUE = "mobile-notifications";
    private final ConnectionFactory factory;
    private static RabbitClient rabbitClient;

    private RabbitClient() {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
    }

    public static RabbitClient getInstance() {
        synchronized (obj) {
            if (rabbitClient == null) {
                rabbitClient = new RabbitClient();
            }
            return rabbitClient;
        }
    }

    public <T> void sendEventToQueue(T model, String queue) {
        CompletableFuture.runAsync(() -> send(model, queue));
    }

    protected <T> void send(T model, String queue) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            System.out.println("RABBITMQ: Sending event to queue [ " + queue + " ]. " + Jsons.toJson((LookupResult)model));

            channel.basicPublish("", queue, null, Jsons.toJson((LookupResult)model).getBytes());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
