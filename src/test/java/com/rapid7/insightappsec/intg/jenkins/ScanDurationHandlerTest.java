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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScanDurationHandler.class)
public class ScanDurationHandlerTest {

    @Mock
    private ScanApi scanApi;

    @Mock
    private InsightAppSecLogger logger;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private String scanId = UUID.randomUUID().toString();

    @Before
    public void setup() {
        PowerMockito.mockStatic(System.class);
    }

    // SCAN PENDING

    @Test
    public void test_handleMaxScanPendingDuration_notApplicable() {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_SUBMITTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.SCANNED);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanPendingDuration_applicable_durationNotExceeded() {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) - 1; // current time less than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded_noScanIdSet() {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        exception.expect(NullPointerException.class);
        exception.expectMessage("Scan ID must not be null");

        // when
        wth.handleMaxScanPendingDuration(null, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded_errorInvokingCancel() throws IOException {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        given(scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL))).willThrow(IOException.class);

        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s",
                                        ScanAction.Action.CANCEL,
                                        scanId));

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded_non200InvokingCancel() throws IOException {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        HttpResponse response = mockSubmitScanAction(ScanAction.Action.CANCEL, 422);

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s. Response %n %s",
                                ScanAction.Action.CANCEL,
                                scanId,
                                response));

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);

        // then
        // exception expected
    }

    @Test(expected = WaitTimeExceededException.class)
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded() throws IOException {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        mockSubmitScanAction(ScanAction.Action.CANCEL, 200);

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));
    }

    // SCAN EXECUTION

    @Test
    public void test_handleMaxExecutionDuration_notApplicable() {
        // given
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, null, null, maxScanExecutionDurationMillis);

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.SCANNED);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationNotExceeded() {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, null, null, maxScanExecutionDurationMillis);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) - 1; // current time less than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.COMPLETE);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationExceeded_noScanIdSet() {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, maxScanExecutionDurationMillis);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) + 1; // current time more than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        exception.expect(NullPointerException.class);
        exception.expectMessage("Scan ID must not be null");

        // when
        wth.handleMaxScanExecutionDuration(null, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationExceeded_errorInvokingStop() throws IOException {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, 0L);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) + 1; // current time more than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        given(scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP))).willThrow(IOException.class);

        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s",
                                ScanAction.Action.STOP,
                                scanId));

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationExceeded_non200InvokingStop() throws IOException {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, 0L);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) + 1; // current time more than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        HttpResponse response = mockSubmitScanAction(ScanAction.Action.STOP, 422);

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage(format("Error occurred submitting scan action %s for scan with id %s. Response %n %s",
                                ScanAction.Action.STOP,
                                scanId,
                                response));

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.RUNNING);

        // then
        // exception expected
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationExceeded() throws IOException {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, 0L);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) + 1; // current time more than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        mockSubmitScanAction(ScanAction.Action.STOP, 200);

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.RUNNING);

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
