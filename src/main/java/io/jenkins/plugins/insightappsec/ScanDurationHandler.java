package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.api.scan.Scan;
import io.jenkins.plugins.insightappsec.api.scan.ScanAction;
import io.jenkins.plugins.insightappsec.api.scan.ScanApi;
import io.jenkins.plugins.insightappsec.exception.DurationExceededException;

public class ScanDurationHandler {

    private final BuildAdvanceIndicator buildAdvanceIndicator;
    private final ScanApi scanApi;
    private final InsightAppSecLogger logger;

    private final Long buildStartTimeMillis;
    private Long scanExecutionStartTimeMillis;

    private final Long maxScanPendingDurationMillis;
    private final Long maxScanExecutionDurationMillis;

    private boolean stopInvoked = false;

    public ScanDurationHandler(BuildAdvanceIndicator buildAdvanceIndicator,
                               ScanApi scanApi,
                               InsightAppSecLogger logger,
                               Long buildStartTimeMillis,
                               Long maxScanPendingDurationMillis,
                               Long maxScanExecutionDurationMillis) {
        this.buildAdvanceIndicator = buildAdvanceIndicator;
        this.scanApi = scanApi;
        this.logger = logger;
        this.buildStartTimeMillis = buildStartTimeMillis;
        this.maxScanPendingDurationMillis = maxScanPendingDurationMillis;
        this.maxScanExecutionDurationMillis = maxScanExecutionDurationMillis;
    }

    void handleMaxScanPendingDuration(String scanId,
                                      Scan.ScanStatus scanStatus) {
        if (maxScanPendingDurationMillis == null) {
            return;
        }

        if (scanStatus.equals(Scan.ScanStatus.PENDING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_STARTED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (durationHasBeenExceeded(buildStartTimeMillis, maxScanPendingDurationMillis)) {
                logger.log("Max scan pending duration has been exceeded, cancelling scan");

                scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));

                throw new DurationExceededException();
            }
        }
    }

    void handleMaxScanExecutionDuration(String scanId,
                                        Scan.ScanStatus scanStatus) {
        if (maxScanExecutionDurationMillis == null) {
            return;
        }

        if (scanStatus.equals(Scan.ScanStatus.RUNNING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (stopInvoked) { // scan has not yet transitioned from running -> stopping
                return;
            }

            initScanStartTimeIfRequired();

            if (durationHasBeenExceeded(scanExecutionStartTimeMillis, maxScanExecutionDurationMillis)) {
                logger.log("Max scan execution duration has been exceeded, stopping scan");

                scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));

                stopInvoked = true;
            }
        }
    }

    // HELPERS

    private void initScanStartTimeIfRequired() {
        if (scanExecutionStartTimeMillis == null) {
            scanExecutionStartTimeMillis = System.currentTimeMillis();
        }
    }

    private boolean durationHasBeenExceeded(long initialTime,
                                            long duration) {
        return (initialTime + duration) < System.currentTimeMillis();
    }

}
