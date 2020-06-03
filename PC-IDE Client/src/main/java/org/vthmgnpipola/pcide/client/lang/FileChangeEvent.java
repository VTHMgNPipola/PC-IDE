package org.vthmgnpipola.pcide.client.lang;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public record FileChangeEvent(Path path, WatchEvent.Kind<?> eventKind) {
}
