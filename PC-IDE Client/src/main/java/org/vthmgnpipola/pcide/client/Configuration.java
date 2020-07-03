package org.vthmgnpipola.pcide.client;

import com.formdev.flatlaf.FlatIntelliJLaf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.lang.FileSystemWatcher;
import org.vthmgnpipola.pcide.client.lang.PseudoCodeInterpreter;
import org.vthmgnpipola.pcide.client.lang.RabbitMQPseudoCodeInterpreter;
import org.vthmgnpipola.pcide.client.lang.SocketPseudoCodeInterpreter;

/**
 * This class holds the settings of this client. when the {@link #load()} method is called, the {@code client
 * .properties} file is processed and the application is configured.
 */
public class Configuration {
    private static final Configuration instance = new Configuration();

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private Properties configuration;
    private ResourceBundle language;

    private Path projectsPath;

    private PseudoCodeInterpreter interpreter;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    /**
     * Loads the {@code client.properties} file and setups some areas of the application, such as, for example, whether the
     * code will be interpreted in a server or locally.
     */
    public void load() throws IOException {
        logger.debug("Processing client.properties...");
        configuration = new Properties();
        try {
            configuration.load(Objects.requireNonNull(Configuration.class.getClassLoader()
                    .getResourceAsStream("client.properties")));
        } catch (IOException e) {
            logger.error("Failed to load client.properties!");
            logger.error(e.getMessage());
            System.exit(-1);
        }

        logger.debug("Installing FlatLaf...");
        boolean result = FlatIntelliJLaf.install();
        if (!result) {
            logger.warn("Unable to install FlatLaf! Default Java LaF will be used.");
        }

        logger.debug("Loading resource bundle...");
        if (configuration.containsKey("language")) {
            Locale.setDefault(Locale.forLanguageTag(configuration.getProperty("language")));
        }
        language = ResourceBundle.getBundle("gui", Locale.getDefault());

        logger.debug("Checking projects folder...");
        // I could put a default value into configuration.getProperty, but since I also need to check if the
        // projects.path key exists I'm not going to do that.
        String pathStr;
        if (configuration.containsKey("projects.path")) {
            pathStr = configuration.getProperty("projects.path");
        } else {
            pathStr = System.getProperty("user.home") + "/PCIDEProjects/";
            logger.trace("Using default projects folder (" + pathStr + ").");
        }
        projectsPath = Path.of(pathStr);
        if (!Files.exists(projectsPath)) {
            try {
                Files.createDirectories(projectsPath);
            } catch (IOException e) {
                logger.error("Error creating projects folder!");
                logger.error(e.getMessage());
                System.exit(-1);
            }
        }

        logger.debug("Creating PseudoCodeInterpreter...");
        String serverMode = configuration.getProperty("server.mode", "socket");
        String address = configuration.getProperty("server.address", "127.0.0.1");
        int port = Integer.parseInt(configuration.getProperty("server.port", "5487"));
        if (serverMode.equalsIgnoreCase("socket")) {
            logger.debug("Socket mode selected.");
            interpreter = new SocketPseudoCodeInterpreter(address, port);
        } else if (serverMode.equalsIgnoreCase("rabbitmq")) {
            logger.debug("RabbitMQ mode selected.");
            interpreter = new RabbitMQPseudoCodeInterpreter(address, port);
        } else {
            logger.error("Invalid server mode selected!");
            System.exit(-1);
        }

        logger.debug("Starting FileSystemWatcher...");
        FileSystemWatcher.getInstance().start();
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public ResourceBundle getLanguage() {
        return language;
    }

    public Path getProjectsPath() {
        return projectsPath;
    }

    public PseudoCodeInterpreter getInterpreter() {
        return interpreter;
    }
}
