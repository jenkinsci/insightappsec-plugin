package com.rapid7.insightappsec.intg.jenkins;

public class InsightAppSecScanStepBuilder {
    
    private String scanConfigId;
    private String buildAdvanceSelector;

    public static InsightAppSecScanStepBuilder builder() {
        return new InsightAppSecScanStepBuilder();
    }

    public InsightAppSecScanStepBuilder withScanConfigId(String scanConfigId) {
        this.scanConfigId = scanConfigId;
        return this;
    }

    public InsightAppSecScanStepBuilder withBuildAdvanceSelector(String buildAdvanceSelector) {
        this.buildAdvanceSelector = buildAdvanceSelector;
        return this;
    }

    public InsightAppSecScanStep build() {
        return new InsightAppSecScanStep(scanConfigId, buildAdvanceSelector);
    }

}
