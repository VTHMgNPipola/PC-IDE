package org.vthmgnpipola.pcide.interpreter.command;

/**
 * A request is an action received by a client that will be executed, and any results are going to be sent back to
 * the caller.
 */
public class ClientRequest {
    private String caller;
    private String request;
    private Object obj;

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
