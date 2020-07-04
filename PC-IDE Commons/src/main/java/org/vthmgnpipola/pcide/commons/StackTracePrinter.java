package org.vthmgnpipola.pcide.commons;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTracePrinter {
    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
