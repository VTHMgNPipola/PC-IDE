package org.vthmgnpipola.pcide.interpreter.command;

import java.util.concurrent.Flow;

public interface RequestReceiver extends Flow.Publisher<ClientRequest>, Runnable {
}
