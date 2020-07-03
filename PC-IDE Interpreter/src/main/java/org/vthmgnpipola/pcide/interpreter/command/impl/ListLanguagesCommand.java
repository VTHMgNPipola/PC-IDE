package org.vthmgnpipola.pcide.interpreter.command.impl;

import java.util.ArrayList;
import java.util.List;
import org.vthmgnpipola.pcide.commons.ClientRequest;
import org.vthmgnpipola.pcide.commons.Language;
import org.vthmgnpipola.pcide.interpreter.command.Command;

public class ListLanguagesCommand extends Command {
    public ListLanguagesCommand() {
        super("list_languages");
    }

    @Override
    public Object run(ClientRequest request) {
        List<Language> result = new ArrayList<>();

        return result;
    }
}
