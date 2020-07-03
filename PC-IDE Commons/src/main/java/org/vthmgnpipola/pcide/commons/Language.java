package org.vthmgnpipola.pcide.commons;

import java.io.Serializable;

/**
 * This is a record class, that represents a language that the interpreter can use to interpret code written in the
 * client.
 */
public class Language implements Serializable {
    private static final long serialVersionUID = -3925379083724696339L;

    private String name;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return name + " - " + version;
    }
}
