package org.vthmgnpipola.pcide.interpreter.command;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a socketed {@link RequestReceiver}. This request receiver receives client request from a socket that it is
 * listening to. This request receiver should only be used when RabbitMQ isn't installed in the machine the
 * interpreter is running, such as when running the interpreter in the same machine as the client.
 */
public class SocketRequestReceiver implements RequestReceiver, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(SocketRequestReceiver.class);

    private Flow.Subscriber<? super ClientRequest> subscriber;
    private ServerSocket serverSocket;

    public SocketRequestReceiver(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {
            Socket connection = serverSocket.accept();

            new Thread(() -> {
                try {
                    ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
                    while (connection.isConnected()) {
                        String str = (String) getObjectOfType(ois, String.class);
                        Object obj = ois.readObject();
                        ClientRequest request = new ClientRequest();
                        request.setCaller(connection.getInetAddress().getHostName());
                        request.setRequest(str);
                        request.setObj(obj);
                        subscriber.onNext(request);
                    }
                } catch (Exception e) {
                    logger.error("Error receiving requests from client at '" + connection.getInetAddress().getHostName() + "'!");
                    logger.error(e.getMessage());
                }
            }).start();
        } catch (IOException e) {
            logger.error("Error receiving requests from clients!");
            logger.error(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ClientRequest> subscriber) {
        logger.info("Subscriber of request receiver changing!");
        this.subscriber = subscriber;
    }

    private static Object getObjectOfType(ObjectInputStream ois, Class<?> type) throws IOException, ClassNotFoundException {
        Object obj = ois.readObject();
        if (!obj.getClass().equals(type)) {
            throw new RuntimeException("Received object is not of type '" + type.getName() + "'!");
        }
        return obj;
    }
}
