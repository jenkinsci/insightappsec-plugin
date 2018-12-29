package com.rapid7.insightappsec.intg.jenkins;

import java.util.UUID;

public class InsightAppSecScanStepModels {

    public static InsightAppSecScanStepBuilder anInsightAppSecScanStep() {
        return InsightAppSecScanStepBuilder.builder();
    }

    public static InsightAppSecScanStepBuilder aCompleteInsightAppSecScanStep() {
        return anInsightAppSecScanStep().withScanConfigId(UUID.randomUUID().toString())
                                        .withBuildAdvanceSelector(InsightAppSecScanStep.BuildAdvanceIndicator.SCAN_COMPLETED.name())
                                        .withRegion(InsightAppSecScanStep.Region.US.name())
                                        .withCredentialsId("Test API Key ID")
                                        .withStoreScanResults(true)
                                        .setMaxScanStartWaitTime("0d 0h 30m")
                                        .setMaxScanRuntime("0d 0h 30m");
    }
}
