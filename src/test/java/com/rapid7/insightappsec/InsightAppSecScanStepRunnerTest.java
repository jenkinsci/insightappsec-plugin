package com.rapid7.insightappsec;

import com.rapid7.insightappsec.api.SafeLogger;
import com.rapid7.insightappsec.api.scan.ScanApi;
import com.rapid7.insightappsec.exception.ScanSubmissionFailedException;
import com.rapid7.insightappsec.mock.MockHttpResponse;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InsightAppSecScanStepRunnerTest {

    @Mock
    private ScanApi scanApi;

    @Mock
    private SafeLogger logger;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private InsightAppSecScanStepRunner runner;

    @Before
    public void before() {
        runner.setLogger(logger);
    }

    @Test
    public void run_scanSubmit_201() throws IOException {
        // given
        String scanConfigId = UUID.randomUUID().toString();

        HttpResponse response = MockHttpResponse.create(201);
        given(scanApi.submitScan(scanConfigId)).willReturn(response);

        // when
        runner.run(scanConfigId);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Response: %n %s", response);
    }

    @Test
    public void run_scanSubmit_non201() throws IOException {
        // given
        String scanConfigId = UUID.randomUUID().toString();
        HttpResponse response = MockHttpResponse.create(400);

        given(scanApi.submitScan(scanConfigId)).willReturn(response);

        exception.expect(ScanSubmissionFailedException.class);
        exception.expectMessage(format("Error occurred submitting scan. Response %n %s", response));

        // when
        runner.run(scanConfigId);

        // then
        // exception expected
    }

    @Test
    public void run_scanSubmit_IOException() throws IOException {
        // given
        String scanConfigId = UUID.randomUUID().toString();

        given(scanApi.submitScan(scanConfigId)).willThrow(new IOException("IOException"));

        exception.expect(ScanSubmissionFailedException.class);
        exception.expectMessage("Error occurred submitting scan");

        // when
        runner.run(scanConfigId);

        // then
        // exception expected
    }

}