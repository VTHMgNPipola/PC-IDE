package org.vthmgnpipola.pcide.interpreter.command.impl;

import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.interpreter.Configuration;
import org.vthmgnpipola.pcide.interpreter.command.Command;

public class HasTasksCommand extends Command {
    public HasTasksCommand() {
        super("has_tasks");
    }

    @Override
    public Object run(ClientRequest request) {
        return Boolean.parseBoolean(Configuration.getInstance().getConfiguration().getProperty("tasks.enabled",
                "false"));
    }
}
