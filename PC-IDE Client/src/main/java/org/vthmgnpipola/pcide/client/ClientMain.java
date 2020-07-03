package org.vthmgnpipola.pcide.client;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vthmgnpipola.pcide.client.gui.ProjectDashboard;
import org.vthmgnpipola.pcide.client.lang.FileSystemWatcher;

/**
 * This is the client side of PC-IDE (PseudoCode Integrated Development Environment).
 * The program executes as follows:
 * <ol>
 *     <li>Reads configuration file {@code client.properties};</li>
 *     <li>Connects to server if a server was defined in {@code client.properties}, tries to update the embedded
 *     interpreter if it isn't;</li>
 *     <li>Opens project dashboard, where users can CRUD projects, filtered by author;</li>
 *     <li>When closed, finishes connection with server, if there is any, and then exits.</li>
 * </ol>
 */
public class ClientMain {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) throws IOException {
        // This is the most appropriate name here
        String coolAsciiArt =
                "    ____  ______     ________  ______\n" +
                "   / __ \\/ ____/    /  _/ __ \\/ ____/\n" +
                "  / /_/ / /  ______ / // / / / __/\n" +
                " / ____/ /__/_____// // /_/ / /___\n" +
                "/_/    \\____/    /___/_____/_____/";
        System.out.println(coolAsciiArt);

        logger.info("Starting up PC-IDE Client...");

        Configuration.getInstance().load();

        logger.info("Successful initialization! Opening Project Dashboard...");
        SwingUtilities.invokeLater(() -> {
            logger.info("Starting GUI...");
            ProjectDashboard dashboard = new ProjectDashboard();
            logger.info("Making dashboard visible...");
            dashboard.setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Configuration.getInstance().getInterpreter().close();

                FileSystemWatcher.getInstance().close();
            } catch (IOException e) {
                logger.error("Unable to close client!");
                logger.error(e.getMessage());
            }
        }));
    }
}
