package org.vthmgnpipola.pcide.interpreter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterpreterMain {
    private static final Logger logger = LoggerFactory.getLogger(InterpreterMain.class);

    public static void main(String[] args) throws IOException {
        String asciiArt = "    ____  ______     ________  ______   ____      __                            __           \n" +
                "   / __ \\/ ____/    /  _/ __ \\/ ____/  /  _/___  / /____  _________  ________  / /____  _____\n" +
                "  / /_/ / /  ______ / // / / / __/     / // __ \\/ __/ _ \\/ ___/ __ \\/ ___/ _ \\/ __/ _ \\/ ___/\n" +
                " / ____/ /__/_____// // /_/ / /___   _/ // / / / /_/  __/ /  / /_/ / /  /  __/ /_/  __/ /    \n" +
                "/_/    \\____/    /___/_____/_____/  /___/_/ /_/\\__/\\___/_/  / .___/_/   \\___/\\__/\\___/_/     \n" +
                "                                                           /_/";
        System.out.println(asciiArt);

        logger.info("Starting PC-IDE Interpreter...");

        Configuration.getInstance().load();

        new Thread(() -> {
            logger.debug("Starting RequestReceiver...");
            Configuration.getInstance().getReceiver().run();
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Configuration.getInstance().getReceiver().close();
            } catch (IOException e) {
                logger.error("Error closing PC-IDE Interpreter!");
                logger.error(e.getMessage());
            }
        }));
    }
}
