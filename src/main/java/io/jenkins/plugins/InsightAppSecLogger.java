package io.jenkins.plugins;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class InsightAppSecLogger {

    private PrintStream printStream;
    private Supplier<String> timestampSupplier = () -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());

    public InsightAppSecLogger(PrintStream printStream) {
        requireNonNull(printStream, "PrintStream must not be null");
        this.printStream = printStream;
    }

    public InsightAppSecLogger(PrintStream printStream,
                               Supplier<String> timestampSupplier) {
        requireNonNull(printStream, "PrintStream must not be null");
        requireNonNull(timestampSupplier, "Supplier must not be null");
        this.printStream = printStream;
        this.timestampSupplier = timestampSupplier;
    }

    public void log(String string) {
        log(string, "");
    }

    public void log(String template,
                    Object ... params) {
        printStream.println(String.format("[iAS - %s] %s", timestampSupplier.get(),
                                                           String.format(template, (Object[]) params)));
    }

}
