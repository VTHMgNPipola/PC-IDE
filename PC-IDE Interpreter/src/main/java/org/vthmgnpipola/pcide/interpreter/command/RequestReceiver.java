package org.vthmgnpipola.pcide.interpreter.command;

import java.io.Closeable;
import java.util.concurrent.Flow;
import org.vthmgnpipola.pcide.commons.ClientRequest;

public interface RequestReceiver extends Flow.Publisher<ClientRequest>, Runnable, Closeable {
}
