package com.rapid7.insightappsec.intg.jenkins.api;

import java.io.PrintStream;

import static java.util.Objects.requireNonNull;

public class InsightAppSecLogger {

    private PrintStream printStream;

    public InsightAppSecLogger(PrintStream printStream) {
        requireNonNull(printStream, "PrintStream must not be null");
        this.printStream = printStream;
    }

    public void log(String string) {
        printStream.println("[iAS] " + string);
    }

    public void log(String template,
                    Object ... params) {
        printStream.println("[iAS] " + String.format(template, (Object[]) params));
    }

}
