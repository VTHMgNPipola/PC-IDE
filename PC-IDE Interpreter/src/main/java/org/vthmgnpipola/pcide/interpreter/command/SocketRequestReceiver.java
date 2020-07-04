package org.vthmgnpipola.pcide.interpreter.command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.commons.StackTracePrinter;

/**
 * This is a socketed {@link RequestReceiver}. This request receiver receives client request from a socket that it is
 * listening to. This request receiver should only be used when RabbitMQ isn't installed in the machine the
 * interpreter is running, such as when running the interpreter in the same machine as the client.
 */
public class SocketRequestReceiver implements RequestReceiver {
    private static final Logger logger = LoggerFactory.getLogger(SocketRequestReceiver.class);

    private Flow.Subscriber<? super ClientRequest> subscriber;
    private final ServerSocket serverSocket;

    public SocketRequestReceiver(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                logger.debug("Waiting for connection...");
                Socket connection = serverSocket.accept();
                logger.info(String.format("Client %s connected!", connection.getInetAddress().getHostName()));

                new Thread(() -> {
                    ObjectInputStream ois;
                    ObjectOutputStream oos;
                    try {
                        oos = new ObjectOutputStream(connection.getOutputStream());
                        oos.flush();
                        ois = new ObjectInputStream(connection.getInputStream());

                        int errors = 0;
                        int maxErrors = 5; // The server will stop when more than 5 errors are thrown in the same second
                        long lastErrorTime = 0;
                        while (connection.isConnected() && errors <= maxErrors) {
                            // Having the try-catch inside the loop prevents the server from stopping to receive client
                            // requests because of an error
                            try {
                                String str = (String) ois.readObject();
                                Object obj = ois.readObject();
                                ClientRequest request = new SocketClientRequest(str, obj, oos);
                                if (request.getRequest().equals("disconnect")) {
                                    logger.info("Client requested to disconnect!");
                                    break;
                                }
                                subscriber.onNext(request);
                            } catch (Exception e) {
                                long currentErrorTime = System.currentTimeMillis();
                                if (currentErrorTime - lastErrorTime > 1000) {
                                    errors = 0;
                                }
                                lastErrorTime = currentErrorTime;
                                errors++;
                                logger.error("Error receiving requests from client at '" + connection.getInetAddress().getHostName() + "'!");
                                logger.error(StackTracePrinter.getStackTraceAsString(e));
                            }
                        }
                        logger.info(String.format("Client at %s disconnected.",
                                connection.getInetAddress().getHostName()));
                    } catch (IOException e) {
                        logger.error(String.format("Error creating input stream for connected client at %s!",
                                connection.getInetAddress().getHostName()));
                    }
                }).start();
            }
        } catch (IOException e) {
            logger.error("Error receiving requests from clients!");
            logger.error(StackTracePrinter.getStackTraceAsString(e));
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

    private static class SocketClientRequest extends ClientRequest {
        private static final long serialVersionUID = 8143244881956062767L;
        private final ObjectOutputStream oos;

        public SocketClientRequest(String request, Object obj, ObjectOutputStream oos) {
            super(request, obj);
            this.oos = oos;
        }

        @Override
        public void sendResponse(Object object) {
            try {
                oos.writeObject(object);
            } catch (IOException e) {
                logger.error("Error sending response to client!");
                logger.error(StackTracePrinter.getStackTraceAsString(e));
            }
        }
    }
}
