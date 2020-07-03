package org.vthmgnpipola.pcide.interpreter;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.interpreter.command.RequestExecutor;
import org.vthmgnpipola.pcide.interpreter.command.RequestReceiver;
import org.vthmgnpipola.pcide.interpreter.command.SocketRequestReceiver;

public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final Configuration instance = new Configuration();

    private Properties configuration;

    private RequestReceiver receiver;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    public void load() throws IOException {
        logger.debug("Configuring interpreter...");

        logger.debug("Loading properties...");
        configuration = new Properties();
        try {
            configuration.load(Objects.requireNonNull(Configuration.class.getClassLoader()
                    .getResourceAsStream("interpreter.properties")));
        } catch (IOException e) {
            logger.error("Failed to load interpreter.properties!");
            logger.error(e.getMessage());
            System.exit(-1);
        }
        logger.debug("Properties loaded successfully.");

        logger.debug("Creating RequestExecutor and RequestReceiver");
        RequestExecutor executor = new RequestExecutor();

        String mode = configuration.getProperty("server.mode", "socket");
        int port = Integer.parseInt(configuration.getProperty("server.port", "5487"));
        if (mode.equalsIgnoreCase("socket")) {
            logger.debug("Socket mode selected.");
            receiver = new SocketRequestReceiver(port);
        }
        receiver.subscribe(executor);
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public RequestReceiver getReceiver() {
        return receiver;
    }
}
