package com.rapid7.insightappsec.intg.jenkins.api;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.function.Supplier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InsightAppSecLoggerTest {

    @Mock
    private PrintStream printStream;

    private Supplier<String> timestampSupplier = () -> "2019-01-07T12:23:40.003";

    private InsightAppSecLogger logger;

    @Before
    public void setup() {
        logger = new InsightAppSecLogger(printStream, timestampSupplier);
    }

    @Test
    public void log() {
        // given
        String logString = "test";

        // when
        logger.log(logString);

        // then
        verify(printStream, times(1)).println("[iAS - " + timestampSupplier.get() + "] test");
    }

    @Test
    public void log_withTemplateAndArgs() {
        // given
        String template = "test %s %s";
        String arg1 = "1";
        String arg2 = "2";

        // when
        logger.log(template, arg1, arg2);

        // then
        verify(printStream, times(1)).println("[iAS - " + timestampSupplier.get() + "] test 1 2");
    }
}