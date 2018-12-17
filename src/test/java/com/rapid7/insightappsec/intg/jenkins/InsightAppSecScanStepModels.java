package com.rapid7.insightappsec.intg.jenkins;

import java.util.UUID;

public class InsightAppSecScanStepModels {

    public static InsightAppSecPluginBuilder anInsightAppSecPlugin() {
        return InsightAppSecPluginBuilder.builder();
    }

    public static InsightAppSecPluginBuilder aCompleteInsightAppSecPlugin() {
        return anInsightAppSecPlugin().withScanConfigId(UUID.randomUUID().toString())
                                      .withBuildAdvanceSelector(BuildAdvanceIndicator.SCAN_COMPLETED.name())
                                      .withRegion(Region.US.name())
                                      .withCredentialsId("Test API Key ID");
    }
}
