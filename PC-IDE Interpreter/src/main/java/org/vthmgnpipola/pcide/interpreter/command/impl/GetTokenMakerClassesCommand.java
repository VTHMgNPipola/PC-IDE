package org.vthmgnpipola.pcide.interpreter.command.impl;

import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.interpreter.Configuration;
import org.vthmgnpipola.pcide.interpreter.command.Command;

public class GetTokenMakerClassesCommand extends Command {
    public GetTokenMakerClassesCommand() {
        super("get_token_maker_classes");
    }

    @Override
    public Object run(ClientRequest request) {
        // This will return null if an invalid request is made
        return Configuration.getInstance().getTokenMakerClasses();
    }
}
