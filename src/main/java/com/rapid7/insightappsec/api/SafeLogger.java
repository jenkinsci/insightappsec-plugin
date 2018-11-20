package com.rapid7.insightappsec.api;

import java.io.PrintStream;

import static java.util.Objects.requireNonNull;

public class SafeLogger {

    private PrintStream printStream;

    public SafeLogger(PrintStream printStream) {
        requireNonNull(printStream, "PrintStream must not be null");
        this.printStream = printStream;
    }

    public void log(String string) {
        printStream.println(string);
    }

    public void log(String template,
                    Object ... params) {
        printStream.println(String.format(template, (Object[]) params));
    }

}
