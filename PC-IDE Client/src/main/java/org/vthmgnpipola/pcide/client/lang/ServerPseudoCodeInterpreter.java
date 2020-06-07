package org.vthmgnpipola.pcide.client.lang;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerPseudoCodeInterpreter implements PseudoCodeInterpreter, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ServerPseudoCodeInterpreter.class);

    private static final String QUEUE_NAME = "pcide";

    private String serverAddress;
    private int port;

    private Connection connection;
    private Channel channel;
    private String callbackQueueName;

    // TODO: Implement security between client and server
    public ServerPseudoCodeInterpreter(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(serverAddress);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            callbackQueueName = channel
                    .queueDeclare(QUEUE_NAME, false, false, true, null)
                    .getQueue();
        } catch (IOException | TimeoutException e) {
            logger.error("Unable to connect to RabbitMQ!");
            logger.error(e.getMessage());
        }
    }

    @Override
    public int interpret(String code) {
        return 0;
    }

    @Override
    public List<Language> getAvailableLanguages() {
        assert connection != null && channel != null;

        // ID of the message
        String correlationId = UUID.randomUUID().toString();

        // Actual message
        String message = "list_languages";

        // Properties of the message, specifies correlation ID and the channel to reply to
        BasicProperties properties = new BasicProperties.Builder().correlationId(correlationId)
                        .replyTo(callbackQueueName).build();

        try {
            // Publishes message to queue
            channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());

            // Gets response
            BlockingQueue<List<Language>> response = new ArrayBlockingQueue<>(1);
            channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    // Read response and turn into List<Language> if the correlation ID of the sent and received
                    // message are the same
                    ByteArrayInputStream bais = new ByteArrayInputStream(delivery.getBody());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try {
                        response.offer((List<Language>) ois.readObject());
                    } catch (ClassNotFoundException e) {
                        logger.error("Unable to read object from server (requesting languages)!");
                        logger.error(e.getMessage());
                    }
                }
            }, consumerTag -> {});

            return response.take();
        } catch (IOException | InterruptedException e) {
            logger.error("Error listing available interpreter languages!");
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
        try {
            channel.close();
        } catch (TimeoutException e) {
            logger.error("Unable to close RabbitMQ channel!");
            logger.error(e.getMessage());
        }
    }
}
