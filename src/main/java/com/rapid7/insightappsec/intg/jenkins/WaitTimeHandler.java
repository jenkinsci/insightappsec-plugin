package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanAction;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.exception.WaitTimeExceededException;

public class WaitTimeHandler {

    // scan start time
    private final long maxScanStartWaitTime;
    private final long buildStartTime;

    // scan runtime
    private final long maxScanRuntime;
    private Long scanStartTime;

    private final BuildAdvanceIndicator buildAdvanceIndicator;
    private final ScanApi scanApi;
    private final InsightAppSecLogger logger;

    private boolean stopInvoked = false;

    WaitTimeHandler(BuildAdvanceIndicator buildAdvanceIndicator,
                    long maxScanStartWaitTime,
                    long maxScanRuntime,
                    ScanApi scanApi,
                    InsightAppSecLogger logger) {
        this.buildAdvanceIndicator = buildAdvanceIndicator;
        this.maxScanStartWaitTime = maxScanStartWaitTime;
        this.maxScanRuntime = maxScanRuntime;
        this.buildStartTime = System.nanoTime(); // now
        this.scanApi = scanApi;
        this.logger = logger;
    }

    void handleMaxScanStartWaitTime(String scanId,
                                    ScanStatus scanStatus) {
        if (maxScanStartWaitTime == -1) {
            return;
        }

        if (scanStatus.equals(ScanStatus.PENDING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_STARTED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (waitTimeHasBeenExceeded(buildStartTime, maxScanStartWaitTime)) {
                logger.log("Max scan start wait time has been exceeded, cancelling scan");

                scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));

                throw new WaitTimeExceededException("Max scan start wait time has been exceeded");
            }
        }
    }

    void handleMaxScanRuntime(String scanId,
                              ScanStatus scanStatus) {
        if (maxScanRuntime == -1) {
            return;
        }

        if (scanStatus.equals(ScanStatus.RUNNING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (stopInvoked) { // scan has not yet transitioned from running -> stopping
                return;
            }

            initScanStartTimeIfRequired();

            if (waitTimeHasBeenExceeded(scanStartTime, maxScanRuntime)) {
                logger.log("Max scan runtime has been exceeded, stopping scan");

                scanApi.submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));

                stopInvoked = true;
            }
        }
    }

    // HELPERS

    private void initScanStartTimeIfRequired() {
        if (scanStartTime == null) {
            scanStartTime = System.nanoTime(); // now
        }
    }

    private boolean waitTimeHasBeenExceeded(long initialTime,
                                            long waitTime) {
        return (initialTime + waitTime) < System.nanoTime();
    }

}
