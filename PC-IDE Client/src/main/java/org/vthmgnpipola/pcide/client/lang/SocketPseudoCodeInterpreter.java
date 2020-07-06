package org.vthmgnpipola.pcide.client.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.Language;
import org.vthmgnpipola.pcide.commons.StackTracePrinter;

public class SocketPseudoCodeInterpreter implements PseudoCodeInterpreter {
    private static final Logger logger = LoggerFactory.getLogger(SocketPseudoCodeInterpreter.class);

    private final Socket connection;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    private Map<String, String> tmClassCodes;
    private ByteClassLoader byteClassLoader;

    public SocketPseudoCodeInterpreter(String address, int port) throws IOException {
        connection = new Socket(address, port);
        oos = new ObjectOutputStream(connection.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(connection.getInputStream());
    }

    @Override
    public int interpret(String code) {
        // TODO: Implement this in the server
        return (int) callServer("interpret", code, Integer.class);
    }

    @Override
    public List<Language> getAvailableLanguages() {
        return (List<Language>) callServer("list_languages", null, List.class);
    }

    @Override
    public boolean isTasksEnabled() {
        return (boolean) callServer("has_tasks", null, Boolean.class);
    }

    @Override
    public TokenMaker getTokenMaker(String code) {
        if (tmClassCodes == null) {
            Map<String, byte[]> tmClassesEncoded = (Map<String, byte[]>) callServer("get_token_maker_classes",
                    null, Map.class);

            Map<String, byte[]> tmClassesDecoded = new HashMap<>();
            tmClassCodes = new HashMap<>();
            assert tmClassesEncoded != null;
            for (Map.Entry<String, byte[]> entry : tmClassesEncoded.entrySet()) {
                String[] keyParts = entry.getKey().split("/");
                tmClassCodes.put(keyParts[0], keyParts[1]);
                tmClassesDecoded.put(keyParts[1], entry.getValue());
            }

            byteClassLoader = new ByteClassLoader(new URL[0], this.getClass().getClassLoader(), tmClassesDecoded);
        }

        String className = tmClassCodes.get(code);
        if (className == null) {
            logger.warn("No TokenMaker found for code " + code);
            return null;
        }
        try {
            Class<?> tokenMakerClass = byteClassLoader.findClass(className);
            return (TokenMaker) tokenMakerClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Error creating TokenMaker instance!");
            logger.error(StackTracePrinter.getStackTraceAsString(e));
            return null;
        }
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
            logger.error(StackTracePrinter.getStackTraceAsString(e));
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        // Sends the signal to close the connection to the server
        try {
            oos.writeObject("disconnect");
            oos.writeObject(null);
        } catch(IOException e) {
            logger.error("Error trying to close connection with server");
            logger.error(StackTracePrinter.getStackTraceAsString(e));
        }

        ois.close();
        oos.close();
        connection.close();
    }
}
