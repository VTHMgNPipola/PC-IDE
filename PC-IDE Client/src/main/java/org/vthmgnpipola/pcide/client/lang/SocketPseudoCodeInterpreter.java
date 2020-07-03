package org.vthmgnpipola.pcide.client.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.Language;

public class SocketPseudoCodeInterpreter implements PseudoCodeInterpreter {
    private static final Logger logger = LoggerFactory.getLogger(SocketPseudoCodeInterpreter.class);

    private final Socket connection;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    public SocketPseudoCodeInterpreter(String address, int port) throws IOException {
        connection = new Socket(address, port);
        oos = new ObjectOutputStream(connection.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(connection.getInputStream());
    }

    @Override
    public int interpret(String code) {
        return 0;
    }

    @Override
    public List<Language> getAvailableLanguages() {
        return (List<Language>) callServer("list_languages", null, List.class);
    }

    /**
     * To call a server connected via sockets, the client will send a
     * {@link org.vthmgnpipola.pcide.commons.ClientRequest}. The server will receive both the request and object,
     * and will send a response, that the client will be waiting for.
     *
     * @param str Message to send to the server.
     * @param obj Object that complements the request.
     * @param expectedResponseType The type that is expected as a response from the server.
     * @return Response sent by the server.
     */
    private Object callServer(String str, Object obj, Class<?> expectedResponseType) {
        try {
            // Sends request to server
            oos.writeObject(str);
            oos.writeObject(obj);

            // Waits for response from server
            Object response = ois.readObject();
            if (response != null && !expectedResponseType.isAssignableFrom(response.getClass())) {
                logger.warn(String.format("Received response from server for request '%s', but the wrong type! " +
                        "(received type %s instead of %s)", str, response.getClass().getName(),
                        expectedResponseType.getName()));
                return null;
            }
            return response;
        } catch (IOException | ClassNotFoundException e) {
            logger.error(String.format("Error sending/receiving request '%s' from server!", str));
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        ois.close();
        oos.close();
        connection.close();
    }
}
