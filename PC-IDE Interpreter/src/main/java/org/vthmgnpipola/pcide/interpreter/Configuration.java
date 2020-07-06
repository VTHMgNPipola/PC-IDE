package org.vthmgnpipola.pcide.interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.commons.Language;
import org.vthmgnpipola.pcide.commons.StackTracePrinter;
import org.vthmgnpipola.pcide.interpreter.command.RequestExecutor;
import org.vthmgnpipola.pcide.interpreter.command.RequestReceiver;
import org.vthmgnpipola.pcide.interpreter.command.SocketRequestReceiver;

public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    private static final Configuration instance = new Configuration();

    private Properties configuration;
    private Path home;

    private RequestReceiver receiver;

    private List<Language> languages;
    private Map<String, byte[]> tokenMakerClasses;

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
            logger.error(StackTracePrinter.getStackTraceAsString(e));
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

        home = Path.of(configuration.getProperty("server.home", System.getProperty("user.home")
                + "/.pcinterpreter/")).normalize();
        Files.createDirectories(home);
        logger.debug(String.format("Server home is %s", home.toString()));

        logger.debug("Detecting languages...");
        languages = new ArrayList<>();
        tokenMakerClasses = new HashMap<>();
        reloadLanguages();

        logger.debug(String.format("Loaded %d languages.", languages.size()));
    }

    private void reloadLanguages() throws IOException {
        File languagesDirectory = home.resolve("languages/").toFile();
        logger.trace(String.format("Loading languages is '%s'.", languagesDirectory.toString()));
        File[] langFiles = languagesDirectory.listFiles((dir, name) -> name.endsWith(".zip") ||
                name.endsWith(".jar"));
        if (langFiles == null) { // The array will be null if no .zip/.jar files are found
            langFiles = new File[0];
        }
        for (File f : langFiles) {
            ZipFile file = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = file.entries();

            Language language = null;
            String tokenMakerLocation = null;
            Map<String, ZipEntry> fileEntries = new HashMap<>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (!entry.isDirectory()) {
                    fileEntries.put(entry.getName().replaceAll("/", "."), entry);
                }

                if (entry.getName().equals("language.properties")) {
                    Properties languageProperties = new Properties();
                    languageProperties.load(file.getInputStream(entry));

                    tokenMakerLocation = languageProperties.getProperty("token_maker_location",
                            "MyTokenMaker.class");

                    language = new Language();
                    language.setName(languageProperties.getProperty("name"));
                    language.setVersion(languageProperties.getProperty("version"));
                    language.setCode(languageProperties.getProperty("code"));
                    languages.add(language);
                    logger.debug(String.format("Added language %s %s", language.getName(),
                            language.getVersion()));
                }
            }

            assert language != null;

            ZipEntry tokenMakerEntry = fileEntries.get(tokenMakerLocation);
            if (tokenMakerEntry != null) {
                byte[] tokenMakerClass = file.getInputStream(tokenMakerEntry).readAllBytes();
                tokenMakerClasses.put(language.getCode() + "/" + tokenMakerLocation.substring(0,
                        tokenMakerLocation.length() - 6), tokenMakerClass); // Length - 6 to remove the '.class'
            }

            file.close();
        }
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public Path getHome() {
        return home;
    }

    public RequestReceiver getReceiver() {
        return receiver;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public Map<String, byte[]> getTokenMakerClasses() {
        return tokenMakerClasses;
    }
}
