package com.rapid7.insightappsec;

public class InsightAppSecScanStepBuilder {
    
    private String scanConfigId;

    public static InsightAppSecScanStepBuilder builder() {
        return new InsightAppSecScanStepBuilder();
    }

    public InsightAppSecScanStepBuilder withScanConfigId(String scanConfigId) {
        this.scanConfigId = scanConfigId;
        return this;
    }

    public InsightAppSecScanStep build() {
        return new InsightAppSecScanStep(scanConfigId);
    }

}
