package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanAction;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanAPIFailureException;
import com.rapid7.insightappsec.intg.jenkins.exception.WaitTimeExceededException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.Objects;

import static java.lang.String.format;

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
                                      ScanStatus scanStatus) {
        if (maxScanPendingDurationMillis == null) {
            return;
        }

        if (scanStatus.equals(ScanStatus.PENDING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_STARTED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (durationHasBeenExceeded(buildStartTimeMillis, maxScanPendingDurationMillis)) {
                logger.log("Max scan pending duration has been exceeded, cancelling scan");

                submitScanAction(scanId, new ScanAction(ScanAction.Action.CANCEL));

                throw new WaitTimeExceededException("Max scan pending duration has been exceeded");
            }
        }
    }

    void handleMaxScanExecutionDuration(String scanId,
                                        ScanStatus scanStatus) {
        if (maxScanExecutionDurationMillis == null) {
            return;
        }

        if (scanStatus.equals(ScanStatus.RUNNING) && (buildAdvanceIndicator.equals(BuildAdvanceIndicator.SCAN_COMPLETED) ||
                                                      buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS))) {

            if (stopInvoked) { // scan has not yet transitioned from running -> stopping
                return;
            }

            initScanStartTimeIfRequired();

            if (durationHasBeenExceeded(scanExecutionStartTimeMillis, maxScanExecutionDurationMillis)) {
                logger.log("Max scan execution duration has been exceeded, stopping scan");

                submitScanAction(scanId, new ScanAction(ScanAction.Action.STOP));

                stopInvoked = true;
            }
        }
    }

    private void initScanStartTimeIfRequired() {
        if (scanExecutionStartTimeMillis == null) {
            scanExecutionStartTimeMillis = System.currentTimeMillis();
        }
    }

    private boolean durationHasBeenExceeded(long initialTime,
                                            long duration) {
        return (initialTime + duration) < System.currentTimeMillis();
    }

    private void submitScanAction(String scanId,
                                  ScanAction scanAction) {
        Objects.requireNonNull(scanId, "Scan ID must not be null");

        try {
            HttpResponse response = scanApi.submitScanAction(scanId, scanAction);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ScanAPIFailureException(format("Error occurred submitting scan action %s for scan with id %s. Response %n %s",
                                                         scanAction.getAction(),
                                                         scanId,
                                                         response));
            }

        } catch (IOException e) {
            throw new ScanAPIFailureException(format("Error occurred submitting scan action %s for scan with id %s",
                                                     scanAction.getAction(),
                                                     scanId), e);
        }
    }

}
