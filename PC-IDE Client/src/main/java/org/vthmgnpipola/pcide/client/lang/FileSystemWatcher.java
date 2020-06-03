package org.vthmgnpipola.pcide.client.lang;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * A FileSystemWatcher is a singleton thread that will keep looking for changes in a working directory.
 *
 * When a change of the correct kind is detected in this working directory, the listener for this directory/file is
 * called.
 */
public class FileSystemWatcher extends Thread implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemWatcher.class);
    private static final FileSystemWatcher instance = new FileSystemWatcher();

    private WatchService watchService;
    private final Map<Path, FileChangeListener> listeners;
    private Path workingDirectory;

    private FileSystemWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            logger.error("Unable to create a WatchService!");
        }
        listeners = new HashMap<>();
    }

    public static FileSystemWatcher getInstance() {
        return instance;
    }

    /**
     * This method will redefine the working directory of the watcher. To do so the directory is registered in the
     * WatchService of the FileSystemWatcher.
     *
     * @param workingDirectory The new working directory for the FileSystemWatcher.
     */
    public void setWorkingDirectory(Path workingDirectory) {
        if (!Files.isDirectory(workingDirectory)) {
            logger.trace("Tried changing working directory of FileSystemWatcher to a regular file.");
            return;
        }

        logger.trace("Working directory of FileSystemWatcher changed to '" + workingDirectory.toString() + "'.");
        try {
            workingDirectory.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_DELETE);
        } catch (IOException e) {
            logger.error("Unable to change working directory!");
            logger.error(e.getMessage());
        }
        this.workingDirectory = workingDirectory;
    }

    /**
     * This will register a new listener to the FileSystemWatcher. The listener will be linked to the file argument,
     * that is, it will only be called when the detected change in the working directory of the FileSystemWatcher is
     * in the specified file.
     *
     * It is important to note that only one listener may be linked to a single file.
     *
     * @param file File to link a listener to.
     * @param listener Listener that will be called when a change in the file is detected.
     */
    public void registerListener(Path file, FileChangeListener listener) {
        Path fullPath = workingDirectory.resolve(file);
        if (Files.exists(fullPath)) {
            listeners.put(file, listener);
        } else {
            logger.warn("Path '" + fullPath.toString() + "' don't exist! Can't register at that " +
                    "path FileChangeListener.");
        }
    }

    /**
     * Removes the listener linked to a file.
     *
     * @param file Removes the listener of this file, if there is any.
     */
    public void unregisterListener(Path file) {
        listeners.remove(file);
    }

    @Override
    public void run() {
        assert watchService != null;

        while (true) {
            WatchKey key;
            try {
                // Wait for changes to happen in working directory.
                key = watchService.take();
            } catch (InterruptedException e) {
                logger.error("Error listening to changes in directory!");
                logger.error(e.getMessage());
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Ignore overflow events
                if (kind == OVERFLOW) {
                    continue;
                }

                // If the event kind is not overflow, then proceed to call the listener associated with the affected
                // path, if there is any.
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path pathToEvent = workingDirectory.resolve(pathEvent.context());
                FileChangeListener listener = null;
                for (Map.Entry<Path, FileChangeListener> entry : listeners.entrySet()) {
                    if (entry.getKey().equals(pathToEvent)) {
                        listener = entry.getValue();
                        break;
                    }
                }
                if (listener != null) {
                    listener.fileChanged(new FileChangeEvent(pathToEvent, kind));
                } else {
                    logger.trace("No listener linked to '" + pathToEvent.toString() + "'.");
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        watchService.close();
        logger.info("FileSystemWatcher closed.");
    }
}
