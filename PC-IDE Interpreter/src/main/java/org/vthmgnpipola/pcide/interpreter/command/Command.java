package org.vthmgnpipola.pcide.interpreter.command;

import org.vthmgnpipola.pcide.commons.ClientRequest;

public abstract class Command {
    protected String command;

    public Command(String command) {
        this.command = command;
    }

    public abstract Object run(ClientRequest request);
}
