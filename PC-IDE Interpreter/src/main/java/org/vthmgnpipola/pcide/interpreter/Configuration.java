package org.vthmgnpipola.pcide.interpreter;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final Configuration instance = new Configuration();

    private Properties configuration;

    private Configuration() {
    }

    public static Configuration getInstance() {
        return instance;
    }

    public void load() {
        logger.debug("Configuring interpreter...");

        logger.trace("Loading properties...");
        configuration = new Properties();
        try {
            configuration.load(Objects.requireNonNull(Configuration.class.getClassLoader()
                    .getResourceAsStream("interpreter.properties")));
        } catch (IOException e) {
            logger.error("Failed to load interpreter.properties!");
            logger.error(e.getMessage());
            System.exit(-1);
        }
        logger.trace("Properties loaded successfully.");

    }
}
