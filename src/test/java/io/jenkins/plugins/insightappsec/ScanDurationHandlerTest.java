package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.api.scan.Scan;
import io.jenkins.plugins.insightappsec.api.scan.ScanAction;
import io.jenkins.plugins.insightappsec.api.scan.ScanApi;
import io.jenkins.plugins.insightappsec.exception.DurationExceededException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ScanDurationHandlerTest {

    @Mock
    private ScanApi scanApi;

    @Mock
    private InsightAppSecLogger logger;

    private String scanId = UUID.randomUUID().toString();

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
        Long buildStartTimeMillis = System.currentTimeMillis();
        Long maxScanPendingDurationMillis = 1000L; // 1 seconds from now

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        // when
        wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded() {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration

        // when
        Assert.assertThrows(DurationExceededException.class, () ->
            wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING)
        );

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
        Long maxScanExecutionDurationMillis = 10000L; // 10 seconds from now

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, null, null, maxScanExecutionDurationMillis);

        // when - will initialize scanExecutionStartTimeMillis to current time
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.COMPLETE);

        // then
        // no exception
    }

    @Test
    public void test_handleMaxExecutionDuration_applicable_durationExceeded() {
        // given - use negative duration to ensure it's already exceeded
        Long maxScanExecutionDurationMillis = -1L; // Already expired

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, maxScanExecutionDurationMillis);

        // when - will initialize scanExecutionStartTimeMillis to current time, duration already exceeded
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.RUNNING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));
    }

}
