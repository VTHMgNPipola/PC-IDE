package org.vthmgnpipola.pcide.interpreter.command;

public abstract class Command {
    protected String command;

    public Command(String command) {
        this.command = command;
    }

    public abstract Object run(ClientRequest request);
}
