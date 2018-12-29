package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanAction;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanAPIFailureException;
import com.rapid7.insightappsec.intg.jenkins.exception.WaitTimeExceededException;
import com.rapid7.insightappsec.intg.jenkins.mock.MockHttpResponse;
import org.apache.http.HttpResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WaitTimeHandlerTest {

    @Mock
    private ScanApi scanApi;

    @Mock
    private InsightAppSecLogger logger;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private String scanId = UUID.randomUUID().toString();

    // SCAN START WAIT TIME

    @Test
    public void test_handleMaxScanStartWaitTime_notApplicable() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_SUBMITTED, 0L, 0L, scanApi, logger);

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.SCANNED);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeNotExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, TimeUnit.MINUTES.toNanos(1), 0L, scanApi, logger);

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeExceeded_noScanIdSet() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        exception.expect(NullPointerException.class);
        exception.expectMessage("Scan ID must not be null");

        // when
        wth.handleMaxScanStartWaitTime(null, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeExceeded_errorInvokingCancel() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        given(scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL))).willThrow(IOException.class);

        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s",
                                        ScanAction.Action.CANCEL,
                                        scanId));

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeExceeded_non200InvokingCancel() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        HttpResponse response = mockSubmitScanAction(ScanAction.Action.CANCEL, 422);

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s. Response %n %s",
                                ScanAction.Action.CANCEL,
                                scanId,
                                response));

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test(expected = WaitTimeExceededException.class)
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeExceeded() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        mockSubmitScanAction(ScanAction.Action.CANCEL, 200);

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));
    }

    // SCAN RUNTIME

    @Test
    public void test_handleMaxScanRuntime_notApplicable() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.SCANNED);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeNotExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, TimeUnit.MINUTES.toNanos(1), scanApi, logger);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.COMPLETE);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeExceeded_noScanIdSet() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_COMPLETED, 0L, 0L, scanApi, logger);

        exception.expect(NullPointerException.class);
        exception.expectMessage("Scan ID must not be null");

        // when
        wth.handleMaxScanRuntime(null, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeExceeded_errorInvokingStop() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_COMPLETED, 0L, 0L, scanApi, logger);

        given(scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP))).willThrow(IOException.class);

        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s",
                                ScanAction.Action.STOP,
                                scanId));

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeExceeded_non200InvokingStop() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_COMPLETED, 0L, 0L, scanApi, logger);

        HttpResponse response = mockSubmitScanAction(ScanAction.Action.STOP, 422);

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s. Response %n %s",
                                ScanAction.Action.STOP,
                                scanId,
                                response));

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeExceeded() throws IOException {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_COMPLETED, 0L, 0L, scanApi, logger);

        mockSubmitScanAction(ScanAction.Action.STOP, 200);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.RUNNING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));
    }

    // HELPERS

    private HttpResponse mockSubmitScanAction(ScanAction.Action action,
                                              int status) throws IOException {
        HttpResponse response = MockHttpResponse.create(status);

        given(scanApi.submitScanAction(scanId, new ScanAction(action))).willReturn(response);

        return response;
    }

}
