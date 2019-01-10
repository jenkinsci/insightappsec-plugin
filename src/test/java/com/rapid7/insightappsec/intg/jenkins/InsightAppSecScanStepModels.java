package com.rapid7.insightappsec.intg.jenkins;

import java.util.UUID;

public class InsightAppSecScanStepModels {

    public static InsightAppSecScanStepBuilder anInsightAppSecScanStep() {
        return InsightAppSecScanStepBuilder.builder();
    }

    public static InsightAppSecScanStepBuilder aCompleteInsightAppSecScanStep() {
        return anInsightAppSecScanStep().withAppId(UUID.randomUUID().toString())
                                        .withScanConfigId(UUID.randomUUID().toString())
                                        .withBuildAdvanceSelector(BuildAdvanceIndicator.SCAN_COMPLETED.name())
                                        .withRegion(Region.US.name())
                                        .withCredentialsId("Test Credentials ID")
                                        .withStoreScanResults(true)
                                        .setMaxScanPendingDuration("0d 0h 30m")
                                        .setMaxScanExecutionDuration("0d 0h 30m");
    }
}
