package org.vthmgnpipola.pcide.commons;

import java.io.Serializable;

/**
 * A request is an action received by a client that will be executed, and any results are going to be sent back to
 * the caller.
 */
public abstract class ClientRequest implements Serializable {
    private static final long serialVersionUID = -546918508014660032L;

    protected String request;
    protected Object obj;

    public ClientRequest(String request, Object obj) {
        this.request = request;
        this.obj = obj;
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

    public abstract void sendResponse(Object object);
}
