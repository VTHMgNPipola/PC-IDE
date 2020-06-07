package org.vthmgnpipola.pcide.client.lang;

import java.util.List;

/**
 * A PseudoCodeInterpreter will communicate with an interpreter, placed in local storage or on the network, and will
 * be able to retrieve information (such as what is the syntax of the language the interpreter is configured to run)
 * and run code written on the client.
 */
public interface PseudoCodeInterpreter {
    int interpret(String code);

    List<Language> getAvailableLanguages();
}
