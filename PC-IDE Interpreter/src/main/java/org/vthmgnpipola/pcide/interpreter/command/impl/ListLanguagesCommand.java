package org.vthmgnpipola.pcide.interpreter.command.impl;

import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.interpreter.Configuration;
import org.vthmgnpipola.pcide.interpreter.command.Command;

public class ListLanguagesCommand extends Command {
    public ListLanguagesCommand() {
        super("list_languages");
    }

    @Override
    public Object run(ClientRequest request) {
        return Configuration.getInstance().getLanguages();
    }
}
