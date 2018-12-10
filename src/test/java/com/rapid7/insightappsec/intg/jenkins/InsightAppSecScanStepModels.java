package com.rapid7.insightappsec.intg.jenkins;

import java.util.UUID;

public class InsightAppSecScanStepModels {

    public static InsightAppSecScanStepBuilder anInsightAppSecScanStep() {
        return InsightAppSecScanStepBuilder.builder();
    }

    public static InsightAppSecScanStepBuilder aCompleteInsightAppSecScanStep() {
        return anInsightAppSecScanStep().withScanConfigId(UUID.randomUUID().toString())
                                        .withBuildAdvanceSelector(BuildAdvanceIndicator.SCAN_COMPLETED.toString());
    }
}
