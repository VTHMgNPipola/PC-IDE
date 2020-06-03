package org.vthmgnpipola.pcide.client.lang;

@FunctionalInterface
public interface FileChangeListener {
    void fileChanged(FileChangeEvent event);
}
