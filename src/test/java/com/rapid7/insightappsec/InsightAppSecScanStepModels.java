package com.rapid7.insightappsec;

import java.util.UUID;

public class InsightAppSecScanStepModels {

    public static InsightAppSecScanStepBuilder anInsightAppSecScanStep() {
        return InsightAppSecScanStepBuilder.builder();
    }

    public static InsightAppSecScanStepBuilder aCompleteInsightAppSecScanStep() {
        return anInsightAppSecScanStep().withScanConfigId(UUID.randomUUID().toString());
    }
}
