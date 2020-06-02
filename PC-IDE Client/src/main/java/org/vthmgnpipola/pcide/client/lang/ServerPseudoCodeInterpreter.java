package org.vthmgnpipola.pcide.client.lang;

import java.io.Closeable;
import java.net.InetAddress;

public class ServerPseudoCodeInterpreter implements PseudoCodeInterpreter, Closeable {
    private InetAddress serverAddress;
    private int port;

    // TODO: Implement security between client and server
    public ServerPseudoCodeInterpreter(InetAddress serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    @Override
    public int interpret(String code) {
        return 0;
    }

    @Override
    public void close() {
    }
}
