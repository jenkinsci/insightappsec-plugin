package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanAction;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.exception.WaitTimeExceededException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        verify(scanApi, times(0)).submitScanAction(eq(scanId), any(ScanAction.class));
    }

    @Test
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeNotExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, TimeUnit.MINUTES.toNanos(1), 0L, scanApi, logger);

        // when
        wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);

        // then
        verify(scanApi, times(0)).submitScanAction(eq(scanId), any(ScanAction.class));
    }

    @Test(expected = WaitTimeExceededException.class)
    public void test_handleMaxScanStartWaitTime_applicable_waitTimeExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        try {
            // when
            wth.handleMaxScanStartWaitTime(scanId, Scan.ScanStatus.PENDING);
        } catch (Exception e) {
            // then
            verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));

            throw e;
        }
    }

    // SCAN RUNTIME

    @Test
    public void test_handleMaxScanRuntime_notApplicable() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, 0L, scanApi, logger);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.SCANNED);

        // then
        verify(scanApi, times(0)).submitScanAction(eq(scanId), any(ScanAction.class));
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeNotExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_STARTED, 0L, TimeUnit.MINUTES.toNanos(1), scanApi, logger);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.COMPLETE);

        // then
        verify(scanApi, times(0)).submitScanAction(eq(scanId), any(ScanAction.class));
    }

    @Test
    public void test_handleMaxScanRuntime_applicable_waitTimeExceeded() {
        // given
        WaitTimeHandler wth = new WaitTimeHandler(BuildAdvanceIndicator.SCAN_COMPLETED, 0L, 0L, scanApi, logger);

        // when
        wth.handleMaxScanRuntime(scanId, Scan.ScanStatus.RUNNING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));
    }

}
