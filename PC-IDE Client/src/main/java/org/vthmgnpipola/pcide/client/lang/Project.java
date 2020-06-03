package org.vthmgnpipola.pcide.client.lang;

import java.nio.file.Path;

public record Project(Path path, String name) {
    @Override
    public String toString() {
        return name;
    }
}
