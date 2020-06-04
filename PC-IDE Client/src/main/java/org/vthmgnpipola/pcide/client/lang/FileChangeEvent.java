package org.vthmgnpipola.pcide.client.lang;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class FileChangeEvent {
    private Path path;
    private WatchEvent.Kind<?> eventKind;

    public FileChangeEvent(Path path, WatchEvent.Kind<?> eventKind) {
        this.path = path;
        this.eventKind = eventKind;
    }

    public Path getPath() {
        return path;
    }

    public WatchEvent.Kind<?> getEventKind() {
        return eventKind;
    }
}
