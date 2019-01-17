package org.jenkinsci.plugins.insightappsec;

import org.jenkinsci.plugins.insightappsec.api.scan.Scan;
import org.jenkinsci.plugins.insightappsec.api.scan.ScanAction;
import org.jenkinsci.plugins.insightappsec.api.scan.ScanApi;
import org.jenkinsci.plugins.insightappsec.exception.DurationExceededException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

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

    @Test(expected = DurationExceededException.class)
    public void test_handleMaxScanPendingDuration_applicable_durationExceeded() {
        // given
        Long buildStartTimeMillis = 0L;
        Long maxScanPendingDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_STARTED, scanApi, logger, buildStartTimeMillis, maxScanPendingDurationMillis, null);

        Long currentTime = (maxScanPendingDurationMillis + buildStartTimeMillis) + 1; // current time more than start + duration
        PowerMockito.when(System.currentTimeMillis()).thenReturn(currentTime);

        try {
            // when
            wth.handleMaxScanPendingDuration(scanId, Scan.ScanStatus.PENDING);
        } catch (Exception e) {
            // then
            verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));
            throw e;
        }
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
    public void test_handleMaxExecutionDuration_applicable_durationExceeded() {
        // given
        Long scanStartTimeMillis = 0L;
        Long maxScanExecutionDurationMillis = 0L;

        ScanDurationHandler wth = new ScanDurationHandler(BuildAdvanceIndicator.SCAN_COMPLETED, scanApi, logger, null, null, 0L);

        Long currentTime = (maxScanExecutionDurationMillis + scanStartTimeMillis) + 1; // current time more than start + duration

        PowerMockito.when(System.currentTimeMillis()).thenReturn(scanStartTimeMillis) // for scan start time init
                                                     .thenReturn(currentTime);

        // when
        wth.handleMaxScanExecutionDuration(scanId, Scan.ScanStatus.RUNNING);

        // then
        verify(scanApi, times(1)).submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));
    }

}
